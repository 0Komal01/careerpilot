"""
Generates the CareerPilot demo dataset.
  python dataset/generate_dataset.py
Outputs:
  backend/src/main/resources/seed/seed.json   (loaded by DataSeeder on first run)
  dataset/*.csv                               (skills, companies, jobs, students, applications for Excel / analysis)
All companies, people and postings are fictional. The RNG seed is fixed, so the output is reproducible.
"""
import csv, json, random
from pathlib import Path

random.seed(42)
ROOT = Path(__file__).resolve().parent.parent
SEED = ROOT / "backend/src/main/resources/seed/seed.json"
CSV_DIR = ROOT / "dataset"

SKILLS = """Java,Spring Boot,Spring Security,Hibernate,JPA,SQL,PostgreSQL,MySQL,MongoDB,REST API,Microservices,Docker,Kubernetes,Git,Maven,JUnit,Mockito,
AWS,Azure,Linux,Python,Pandas,NumPy,Scikit-learn,TensorFlow,PyTorch,Machine Learning,Deep Learning,NLP,Computer Vision,Data Structures,Algorithms,OOP,System Design,
JavaScript,TypeScript,React,Node.js,Express.js,HTML,CSS,Tailwind CSS,Redux,Next.js,Flutter,Dart,Kotlin,C++,Golang,Figma,Power BI,Tableau,MS Excel,Statistics,
Data Analysis,CI/CD,Jenkins,Selenium,Postman,Cybersecurity,Networking,Agile,FastAPI,Django,GraphQL,Kafka,Redis""".replace("\n", "").split(",")

COMPANIES = [
 ("Nimbus Labs","Bengaluru"),("Quantra Systems","Pune"),("Pixelforge Studio","Hyderabad"),("BlueMesh Cloud","Bengaluru"),
 ("Kavach Security","Gurugram"),("Lumen Analytics","Mumbai"),("Orbit Payments","Bengaluru"),("TerraByte Technologies","Ahmedabad"),
 ("Zenith Health Tech","Chennai"),("Cobalt Retail Systems","Pune"),("Sutra Software","Rajkot"),("Vayu Mobility","Hyderabad"),
 ("Aarna FinServe","Mumbai"),("Helix Robotics","Pune"),("Ganga Logistics Tech","Ahmedabad"),("Kiranaa Commerce","Remote")]

# title, required skills, responsibilities, min cgpa range, LPA range, job type
ROLES = [
 ("Java Backend Developer", ["Java","Spring Boot","SQL","REST API","Git","Hibernate"], "build and maintain REST services with Spring Boot|design normalized schemas and tune SQL queries", (6.5,7.5),(6,14),"Full-time"),
 ("Full-Stack Engineer", ["React","JavaScript","Node.js","SQL","REST API","Git"], "ship features across a React front end and Node.js APIs|own pages from design hand-off to production", (6.5,7.5),(6,15),"Full-time"),
 ("Frontend Developer", ["React","TypeScript","HTML","CSS","JavaScript","Git"], "build accessible, responsive interfaces in React|turn Figma designs into reusable components", (6.0,7.0),(5,12),"Full-time"),
 ("Data Analyst", ["SQL","Python","Pandas","MS Excel","Power BI","Statistics"], "turn raw data into weekly business dashboards|run ad-hoc SQL analysis for product and ops teams", (6.5,7.5),(5,11),"Full-time"),
 ("Machine Learning Engineer", ["Python","Machine Learning","Scikit-learn","NumPy","TensorFlow","Data Structures"], "train, evaluate and deploy ML models|build data pipelines and monitor model drift", (7.5,8.5),(8,20),"Full-time"),
 ("DevOps Engineer", ["Docker","Kubernetes","Linux","CI/CD","Git","AWS","Jenkins"], "automate build and deployment pipelines|manage containerised workloads and monitoring", (6.5,7.5),(7,16),"Full-time"),
 ("Software Engineer Trainee", ["Data Structures","Algorithms","OOP","Java","SQL"], "complete a 6-month structured training programme|contribute to production code under a mentor", (6.0,7.0),(4,9),"Full-time"),
 ("QA Automation Engineer", ["Selenium","Java","Postman","JUnit","SQL","Agile"], "write automated UI and API tests|own regression suites and release sign-off", (6.0,7.0),(4,10),"Full-time"),
 ("Flutter Mobile Developer", ["Flutter","Dart","REST API","Git","Redux"], "build cross-platform mobile apps with Flutter|integrate payments, maps and push notifications", (6.0,7.0),(5,12),"Full-time"),
 ("Cloud Engineer", ["AWS","Linux","Networking","Docker","Python","Git"], "design secure, cost-aware AWS infrastructure|write automation scripts for provisioning", (6.5,7.5),(7,15),"Full-time"),
 ("Cybersecurity Analyst", ["Cybersecurity","Networking","Linux","Python","Git"], "monitor alerts and triage security incidents|run vulnerability scans and write remediation notes", (6.5,7.5),(6,14),"Full-time"),
 ("NLP Engineer", ["Python","NLP","PyTorch","Deep Learning","FastAPI","Git"], "fine-tune and serve language models|build evaluation sets for text classification", (7.5,8.5),(9,22),"Full-time"),
 ("Python Backend Developer", ["Python","Django","SQL","REST API","Docker","Git"], "build Django services and background workers|write clean, well-tested APIs", (6.5,7.5),(6,14),"Full-time"),
 ("Business Analyst", ["SQL","MS Excel","Tableau","Agile","Data Analysis"], "translate stakeholder needs into user stories|track KPIs and present findings", (6.5,7.5),(5,10),"Full-time"),
 ("Software Development Intern", ["Data Structures","OOP","Git","SQL"], "work on a real feature with a mentor for 6 months|write tests and fix production bugs", (6.0,7.0),(0.4,0.6),"Internship"),
 ("React Developer Intern", ["React","JavaScript","HTML","CSS","Git"], "build UI components and fix front-end bugs|pair with senior engineers on code reviews", (6.0,7.0),(0.3,0.5),"Internship"),
]
PREFERRED = {"Java":"Docker, Microservices","React":"TypeScript, Redux","Python":"FastAPI, Docker","SQL":"PostgreSQL, indexing basics","AWS":"Terraform","Docker":"Kubernetes"}
LOCS = ["Bengaluru","Pune","Hyderabad","Mumbai","Ahmedabad","Gurugram","Chennai","Remote","Rajkot"]

jobs = []
for i in range(38):
    comp = COMPANIES[i % len(COMPANIES)]
    r = ROLES[(i * 5 + i // len(ROLES)) % len(ROLES)]
    title, req, duties, cg, lpa, jtype = r
    loc = comp[1] if random.random() < .75 else random.choice(LOCS)
    mincg = round(random.uniform(*cg) * 2) / 2
    sal = round(random.uniform(*lpa), 1) if jtype == "Full-time" else round(random.uniform(*lpa), 2)
    skills = req[:] if random.random() < .6 else req[:-1]          # vary list length a little
    gradyear = 2027 if random.random() < .45 else None
    pref = PREFERRED.get(skills[0], "Open-source contributions")
    desc = (f"{comp[0]} is hiring a {title} ({jtype.lower()}) for its {loc} team. "
            f"You will {duties.split('|')[0]} and {duties.split('|')[1]}. "
            f"Required skills: {', '.join(skills)}. Nice to have: {pref}. "
            f"Eligibility: B.Tech / B.E. / MCA with at least {mincg} CGPA"
            f"{', 2027 batch' if gradyear else ''}. Freshers are welcome; we value projects over certificates.")
    days = random.choice([-12, 6, 9, 14, 18, 22, 27, 33, 41, 55, 70]) if i > 3 else random.choice([14, 22, 35])
    jobs.append(dict(ref=f"J{i+1:02d}", company=comp[0], title=title, description=desc, minimumCgpa=mincg, location=loc,
                     salaryLpa=sal, jobType=jtype, deadlineDays=days, graduationYear=gradyear,
                     allowedDegrees="B.Tech,B.E.,MCA", skills=skills))

PERSONAS = {
 "backend":  ["Java","Spring Boot","SQL","REST API","Git","Hibernate","Data Structures","OOP","Docker","Maven","JUnit","MySQL"],
 "fullstack":["React","JavaScript","Node.js","HTML","CSS","SQL","REST API","Git","MongoDB","Express.js","TypeScript","Tailwind CSS"],
 "ml":       ["Python","Machine Learning","Scikit-learn","NumPy","Pandas","TensorFlow","Deep Learning","Data Structures","Statistics","NLP","Git","SQL"],
 "data":     ["SQL","Python","Pandas","MS Excel","Power BI","Statistics","Tableau","Data Analysis","MySQL","Agile"],
 "devops":   ["Docker","Linux","Git","CI/CD","AWS","Jenkins","Kubernetes","Python","Networking","Postman"],
 "mobile":   ["Flutter","Dart","REST API","Git","Kotlin","Redux","JavaScript","OOP"],
}
PEOPLE = [("Aarav Patel","backend",9.1,2027,"B.Tech Computer Engineering"),("Diya Shah","fullstack",8.6,2027,"B.Tech Computer Engineering"),
 ("Rohan Mehta","ml",8.9,2027,"B.E. Information Technology"),("Ananya Iyer","data",8.2,2027,"B.Tech Computer Science"),
 ("Kabir Singh","devops",7.4,2027,"B.Tech Computer Engineering"),("Meera Joshi","backend",7.9,2027,"B.E. Information Technology"),
 ("Vihaan Desai","fullstack",6.8,2027,"B.Tech Computer Science"),("Isha Kapoor","ml",9.3,2026,"B.Tech Computer Engineering"),
 ("Arjun Nair","mobile",7.2,2027,"B.Tech Computer Engineering"),("Sneha Reddy","data",8.0,2027,"MCA"),
 ("Dev Trivedi","backend",6.4,2027,"B.Tech Computer Engineering"),("Pooja Verma","fullstack",8.4,2026,"B.E. Information Technology"),
 ("Yash Solanki","devops",7.7,2027,"B.Tech Computer Science"),("Tanvi Kulkarni","ml",8.7,2027,"B.Tech Computer Engineering")]
COLLEGES = ["Rajkot Institute of Technology","Western India College of Engineering","Saurashtra Technical University","Gujarat Polytechnic and Engineering College"]
PROJ = {"backend":"CareerPilot placement portal (Spring Boot + JWT); Library management REST API with 12 endpoints; Expense tracker using JPA and PostgreSQL",
 "fullstack":"Portfolio site with CMS in React and Node.js; Realtime chat app using WebSockets; E-commerce storefront with Stripe test checkout",
 "ml":"Deepfake image classifier with 94% validation accuracy; Sentiment analysis on 50k reviews; Federated learning demo across 3 simulated clients",
 "data":"Sales dashboard in Power BI for 120k rows; Customer churn analysis in Python; Airline delay EDA with SQL and Pandas",
 "devops":"CI/CD pipeline for a Dockerised app using Jenkins; Kubernetes deployment on a local cluster; Log monitoring stack with alerts",
 "mobile":"Expense splitter app in Flutter (1.2k test installs); Campus events app with push notifications; Weather app using public APIs"}
CERT = {"backend":"Oracle Java SE Associate; NPTEL Database Management Systems","fullstack":"Meta Front-End Developer Certificate; freeCodeCamp Responsive Web Design",
 "ml":"Coursera Machine Learning Specialization; NPTEL Deep Learning","data":"Microsoft Power BI Data Analyst; Google Data Analytics",
 "devops":"AWS Cloud Practitioner; Docker Certified Associate (study track)","mobile":"Google Associate Android Developer (prep); Flutter bootcamp"}

students = []
for i, (name, persona, cg, yr, deg) in enumerate(PEOPLE):
    pool = PERSONAS[persona]
    n = len(pool) if i == 0 else random.randint(max(5, len(pool) - 5), len(pool))   # first student (demo login) has the richest profile
    skills = pool[:n] if i == 0 else random.sample(pool, n)
    if random.random() < .3: skills.append(random.choice(["Agile","Linux","Postman","Algorithms"]))
    first = name.split()[0].lower()
    students.append(dict(name=name, email=f"{first}@careerpilot.dev", college=random.choice(COLLEGES), degree=deg, cgpa=cg,
        graduationYear=yr, phone=f"+91 98{random.randint(10000000,99999999)}", projects=PROJ[persona], certifications=CERT[persona],
        skills=sorted(set(skills)), persona=persona))

def eligible(st, jb):
    return st["cgpa"] >= jb["minimumCgpa"] and (jb["graduationYear"] in (None, st["graduationYear"])) and jb["deadlineDays"] >= 0

def overlap(st, jb):
    have = set(s.lower() for s in st["skills"])
    return sum(1 for s in jb["skills"] if s.lower() in have) / len(jb["skills"])

PATHS = [["APPLIED"], ["APPLIED","SHORTLISTED"], ["APPLIED","SHORTLISTED","TECHNICAL_INTERVIEW"],
         ["APPLIED","SHORTLISTED","TECHNICAL_INTERVIEW","HR_INTERVIEW"], ["APPLIED","SHORTLISTED","TECHNICAL_INTERVIEW","HR_INTERVIEW","SELECTED"], ["REJECTED"]]
applications = []
for si, st in enumerate(students):
    cands = sorted([j for j in jobs if eligible(st, j)], key=lambda j: -overlap(st, j))[:8]
    picks = cands[:2] + random.sample(cands[2:], min(len(cands[2:]), random.randint(0, 2))) if si else cands[:5]
    for k, j in enumerate(picks):
        if si == 0:   # demo student: one application at every stage so the UI looks alive
            status = ["SELECTED","HR_INTERVIEW","TECHNICAL_INTERVIEW","SHORTLISTED","APPLIED"][k % 5]
        else:
            status = random.choice(PATHS)[-1] if overlap(st, j) > .5 else random.choice(["APPLIED","REJECTED","SHORTLISTED"])
        applications.append(dict(student=st["email"], job=j["ref"], status=status, daysAgo=random.randint(2, 40)))

for s in students: s.pop("persona")
seed = dict(admin=dict(email="admin@careerpilot.dev", password="Admin@123"), studentPassword="Student@123",
            skills=SKILLS, companies=[dict(name=n, location=l, website=f"https://{n.lower().replace(' ','')}.example.com") for n, l in COMPANIES],
            jobs=jobs, students=students, applications=applications)
SEED.parent.mkdir(parents=True, exist_ok=True)
SEED.write_text(json.dumps(seed, indent=1), encoding="utf-8")

def write_csv(name, rows):
    with open(CSV_DIR / name, "w", newline="", encoding="utf-8") as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0].keys())); w.writeheader(); w.writerows(rows)
write_csv("skills.csv", [{"name": s} for s in SKILLS])
write_csv("companies.csv", seed["companies"])
write_csv("jobs.csv", [{**j, "skills": "; ".join(j["skills"])} for j in jobs])
write_csv("students.csv", [{**s, "skills": "; ".join(s["skills"])} for s in students])
write_csv("applications.csv", applications)
print(f"skills={len(SKILLS)} companies={len(COMPANIES)} jobs={len(jobs)} students={len(students)} applications={len(applications)}")
