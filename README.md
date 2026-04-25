## 1. Project Scope

**BAC** is an end-to-end Healthcare Service Management and Electronic Visit Verification (EVV). It digitizes the flow from **Individual Support Plan (ISP)** creation to **Real-time Service Delivery** and automated **Medicaid Billing**. The system specifically solves the logistics of matching caregivers (DSPs) to patients while ensuring strict compliance with Pennsylvania ODP/DHS regulations.

---

## 2. System Actors

The system manages interactions across five distinct roles to ensure data integrity and operational oversight.

| Actor           | Responsibility                                                                                        |
| --------------- | ----------------------------------------------------------------------------------------------------- |
| DSP / Caregiver | Uses the mobile app to Check In/Out via GPS, records Daily Notes, and logs medication administration. |
| Office Manager  | Manages staff/patient records, creates weekly schedules, and reviews medication/incident reports.     |
| Finance Admin   | Validates billable units, manages Medicaid claims, and monitors revenue/expenses.                     |
| System Admin    | Configures RBAC, monitors system health, manages API integrations, and office-level settings.         |
| Audit Inspector | External actor (AE/ODP) who accesses the system to review compliance logs and Fire Drill reports.     |

---

## 3. Key Use Cases

- **ISP-Driven Scheduling:** Automatically generates weekly shifts based on the units and service types authorized in the patient's ISP.
- **Geofenced EVV (Electronic Visit Verification):** Restricts staff Check-In to a specific radius of the patient's residence using GPS coordinates.
- **Collision-Free Dispatch:** Prevents staff from being assigned to overlapping shifts or multiple locations at the same time.
- **Medication Administration Record (eMAR):** Digital tracking of scheduled and PRN medication with real-time alerts for missed doses.
- **Multi-Office Data Isolation:** Ensures that data is siloed by office location while allowing global reporting for executive users.

---

## 4. Tech Stack: The "Enterprise" Selection

| Tier         | Technology    | Rationale                                                                                                                                  |
| ------------ | ------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Frontend     | Angular       | Chosen for its robust structure and RxJS streams, essential for complex scheduling grids and multi-office dashboards.                      |
| Backend      | Spring Boot   | Provides a secure, stateless architecture for RBAC and high-concurrency event processing.                                                  |
| Cache / Lock | Redis         | **Essential.** Used for **Distributed Locking** to prevent scheduling overlaps and caching "Hot" ISP unit balances for instant validation. |
| Messaging    | Kafka         | Used for asynchronous workflows: triggering billing claims once a "Daily Note" is submitted and sending automated expiry alerts.           |
| Search/Logs  | Elasticsearch | Handles high-speed searching of historical Incident Reports and Daily Notes during state audits.                                           |

---

## 5. Technical Problems & Solutions

### Problem 1: The "Overlap" Integrity Constraint

**Challenge:** Ensuring a staff member never has two shifts at once across different patients or offices.
**Solution:** Implement a **Redis Distributed Lock** (Redisson). When a manager saves a shift, the system attempts to acquire a lock on the `staff_id` for that specific time block. If the lock fails, the transaction is rejected, preventing data corruption in a multi-server environment.

### Problem 2: Real-time Unit Depletion Accuracy

**Challenge:** A patient has a fixed number of 15-minute "Units" per year. We must prevent over-scheduling in real-time.
**Solution:** Maintain a "Unit Ledger" in **Redis**. As shifts are created or completed, the system performs atomic decrements. This prevents "Race Conditions" where two managers might accidentally use the same last 5 units for different schedules.

### Problem 3: High-Precision Location Verification (EVV)

**Challenge:** Fraud prevention—ensuring staff are actually at the patient's home.
**Solution:** Upon Check-In, the mobile app sends GPS coordinates to a Spring Boot service. The system calculates the distance $d$ between the staff's location and the patient's `residence_location`.
$$d = 2r \arcsin\left(\sqrt{\sin^2\left(\frac{\phi_2 - \phi_1}{2}\right) + \cos(\phi_1) \cos(\phi_2) \sin^2\left(\frac{\lambda_2 - \lambda_1}{2}\right)}\right)$$
If $d > 50 \text{ meters}$, the system flags the unit as "Non-Billable" and sends a Kafka alert to the Manager.

---

## 6. General Requirements

### Functional

- **RBAC:** Role-Based Access Control to mask sensitive SSN/Medical data based on user role.
- **Electronic Signatures:** Mandatory e-signatures for Daily Notes and Medication logs.
- **Offline Mode:** Mobile app must store GPS events when no signal is present and sync once reconnected.

### Non-Functional

- **HIPAA Compliance:** Encryption of all PII/PHI data at rest and in transit.
- **Auditability:** Every change to an ISP or a Billing Claim must be logged with a "Who/What/When" timestamp.
- **Scalability:** Horizontal scaling of Spring Boot instances to handle peak morning Check-In surges.
