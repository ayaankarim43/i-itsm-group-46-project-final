I-ITSM: Intelligent IT Service Management & Incident Resolution System

Project for CS F213 / MAC F212 Object-Oriented Programming, Semester I 2026-27.

I-ITSM manages the full life of an IT incident: report, classify, prioritise, assign, investigate, resolve, verify and close. It calculates priority automatically, assigns technicians with swappable strategies, tracks SLA deadlines in the background, notifies the people involved and records every step in an audit trail.

Group No.: 46
Team: Bhargav Teja Akella (2025ADPS0149H), Borra Monish (2025ADPS0212H), Sirasani Eswar Sai Madhav (2025A7PS0106H), Ayaan Karim Shaikh (2025A7PS0007H)

Tech stack
Java 21, Spring Boot 4.1 (Maven wrapper included)
Spring MVC with Thymeleaf pages (server-rendered UI) and a REST API under /api
Spring Data JPA, with H2 in file mode (the PostgreSQL driver is also included but not used)
Spring Security (BCrypt, form login, HTTP Basic for the API, role-based access)
Spring Mail, used for registration and password-reset codes
How to run

You need JDK 21. From the project folder:

./mvnw spring-boot:run          # macOS / Linux
mvnw.cmd spring-boot:run        # Windows

On macOS or Linux, if ./mvnw says "permission denied", run chmod +x mvnw first (or use sh mvnw spring-boot:run).

You can also open the project in IntelliJ IDEA and run DemoApplication. Then open http://localhost:8080. Stop the application with Ctrl+C.

Mail settings

Registration and password reset email a one-time code. To enable real emails, set these environment variables before starting the app:

Variable	Meaning
MAIL_HOST	SMTP server, for example smtp.gmail.com
MAIL_PORT	SMTP port (default 587)
MAIL_USERNAME	SMTP login
MAIL_PASSWORD	SMTP password or app password

Windows PowerShell:

$env:MAIL_HOST="smtp.gmail.com"; $env:MAIL_USERNAME="you@example.com"; $env:MAIL_PASSWORD="your-app-password"

macOS / Linux:

export MAIL_HOST=smtp.gmail.com MAIL_USERNAME=you@example.com MAIL_PASSWORD=your-app-password

The mail variables are optional. Without them the app still starts and the demo accounts work, but registration and password-reset emails are not delivered.

Database

Data is stored in the data/ folder (H2 file database) and survives restarts. Demo data is created automatically on the first start. It is only added when the database is empty, so to reset everything (for example, to start from a clean demo), stop the app and delete the contents of the data/ folder. The project may include a data/ folder from an earlier run.

Demo accounts

All demo accounts use the password password123.

Role	Name	Email	Details
Employee	Asha	asha@corp.test	Finance department
Employee	Dev	dev@corp.test	Sales department
Technician	Ravi	ravi@corp.test	Skills: Network, Database. 5 years' experience, up to 5 active incidents
Technician	Meera	meera@corp.test	Skills: Application, Cloud. 3 years' experience, up to 4 active incidents
Administrator	Admin	admin@corp.test	Full access

New employee accounts can be created from the Register page (email verification code required).

The demo data also contains three IT services (Payment with business criticality 5, Network 4, Email 2), three categories (Application, Network, Database) and the SLA policies from the project brief:

Priority	Response target	Resolution target
P1	5 minutes	30 minutes
P2	15 minutes	2 hours
P3	1 hour	8 hours
P4	4 hours	24 hours

Payment has a stricter P2 policy (10 minutes and 90 minutes).

What each role can do
Employee: register and log in, report incidents, edit the details of their own incident while it is still NEW (priority is recalculated), see and search only their own incidents, track status and SLA, verify a resolution, read notifications.
Technician: see all incidents, edit the details of an incident while it is still NEW, classify and assign incidents, acknowledge, start work and resolve incidents assigned to them (resolution details are required), close verified incidents, view the dashboard and technician workload.
Administrator: everything a technician can do on any incident, plus reporting incidents, editing SLA policies, switching the assignment strategy and reading the audit log.
Quick demo walkthrough
Log in as asha@corp.test. Choose Report incident: "Payment application unavailable", service Payment, impact 4, urgency 4. The incident is created as NEW with priority P1.
Log out and log in as meera@corp.test. Open the incident and use the next-step button each time: Classify, Assign, Acknowledge, Start work, Resolve (enter resolution details). Skill-based assignment picks Meera because she has the Application skill.
Log in as Asha again, open the incident and choose Verify resolution. Check Notifications.
Log in as Meera and Close the incident.
Log in as admin@corp.test. Open the Dashboard and the Admin page (switch assignment strategy, edit an SLA policy, read the audit log).

To see SLA monitoring, shorten the P1 SLA to 1 and 3 minutes on the Admin page, report a P1 incident, classify it and wait. The monitor runs every 30 seconds and records a warning and then a violation in the incident history.

Features
Incident lifecycle with enforced transitions: NEW, CLASSIFIED, ASSIGNED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, VERIFIED, CLOSED
Priority = impact x urgency x business criticality (P1 at 60 or more, P2 at 30, P3 at 12, otherwise P4)
Two assignment strategies (skill-based and load-based) switchable at runtime, with a per-technician workload cap
SLA tracking: response and resolution deadlines, warning at 80% of the time window, recorded violations
Notifications: in-app (stored) and simulated email (written to the application log)
Editing an incident's description, impact and urgency while it is NEW, with priority recalculated and the change recorded in the history
Audit trail for every state change, with user, time, previous and new state; the administrator page shows the complete log, newest first
Search (web form and API) by ID, status, priority, category, service, technician, SLA status, date range and free text; dashboard and technician workload reports
Intelligent features: smart technician recommendation, SLA risk warnings, automatic category suggestion from the description
Security: BCrypt passwords, role-based and object-level authorisation, input validation, CSRF protection on web forms, email-verified registration and password reset
Design patterns
Pattern	Where
State	Incident lifecycle (IncidentState and one class per status)
Strategy	Priority calculation, technician assignment, category classification
Observer	Audit trail, in-app notifications and simulated email react to incident events
Repository	Spring Data JPA repositories
REST API

The API uses HTTP Basic authentication with the same accounts. Examples:

GET  /api/incidents
GET  /api/incidents/search?priority=P1&status=NEW
POST /api/incidents/{id}/assign
GET  /api/reports/dashboard        (technician or administrator)
GET  /api/admin/sla-policies       (administrator)
GET  /api/auth/me
Testing

The test cases and their results are documented in the project report (Section 10). They were run through the web interface and REST requests.
