# 🏡 Nestya — A collaborative home management platform

Nestya is an open-source platform to help households organize their daily life.  
Share and manage groceries, meals, tasks, and budgets with your home members, all in one place.

---

## ✨ Features
- 👥 Multi-user homes: invite members and collaborate
- 🛒 Shared grocery lists: add, edit, and track items
- 🍲 Meal planner: schedule meals on a calendar
- 📝 Task manager: assign, track, and complete chores
- 💰 Budget tracker: record and analyze household expenses
- 🔔 Real-time updates & notifications
- 📱 Responsive & PWA-ready frontend

---

## 🚀 Tech Stack
- **Backend:** Spring Boot + JWT
- **Frontend:** Vue 3 + Vue Router + PWA
- **Database:** PostgreSQL
- **Deployment:** Docker & Docker Compose

---

## 📦 Getting Started

### Prerequisites
- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)
- Node.js & npm (optional if running frontend separately)

### Clone the repo
```bash
git clone https://github.com/ArKc0s/Nestya.git
cd Nestya
````

### Run locally with Docker

```bash
docker compose up --build
```

* Backend available at: [http://localhost:8080](http://localhost:8080)
* Frontend available at: [http://localhost:5173](http://localhost:5173)

### Run frontend (development)

```bash
cd ui
npm install
npm run dev
```

### Run backend (development)

```bash
cd api
./mvnw spring-boot:run
```

---

## 🗺 Roadmap

* [ ] MVP: authentication, home management, groceries, meals, tasks, budget
* [ ] Real-time sync with WebSockets
* [ ] Advanced budget analytics
* [ ] Push notifications
* [ ] Mobile app (Flutter or React Native)

Check the [project boards](https://github.com/ArKc0s/Nestya/projects) for progress and priorities.

---

## 📖 Documentation

* [API Docs (Swagger)](http://localhost:8080/api/docs) (once backend is running)

---

## 🤝 Contributing

Pull requests are welcome!
Please check the [issues](https://github.com/yourusername/hestia/issues) and follow the contribution guidelines.

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 📬 Contact

Maintainer: [Léo Wadin](mailto:wadinleo@gmail.com)
