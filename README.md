# Grad IQ – PROG7314 Part 2

**Repository:** `prog7314-part2-grad_iq`  
**Tech Stack:** Kotlin (Android), Node.js (Express, MongoDB), Render for hosting the Restful API  
**Author(s):** 
- Jayden Larkins
- Gerhard Lemmer
- Thatho Mokoena
- Mokran Ait Amara

**Date:** 24 September 2025 

---
## Prerequisites:  
- Android studio Meerkat +
- SHA-1 certificate for Google SSO
- Biometrics **(For part 3 only)**
- AGP of 8.10.1

---
## 📌 Overview
RentWise is an application built for the PROG7314 Part 2 assignment.  
It consists of:  

- 📱 **Android (Kotlin)** app for users to interact with the system.  
- 🌐 **Node.js (Express + MongoDB)** backend API for data management, authentication, authorisation and CRUD Functionality.  

The system allows users to **register, log in (JWT/Google Sign-In) and perform CRUD functionailty for property management** once online.  

> ⚠️ Note: The Restful API Code is on the [master branch](https://github.com/VCSTDN2024/prog7314-part2-grad_iq/tree/master).

## ✨ Features
### Android (Kotlin)
- User authentication (JWT / Google Sign-In).  
- Offline support with **Room Database (To be implemented in Part3)**.  
- Automatic sync with backend API **(To be implemented in Part3)**.  
- Property / data management (CRUD).
- Multilingual Support **(To be implemented within Part 3)**  
- AI Chatbot for user queries **(To be implemented within Part 3)**
- Google SSO
- Biometric Authentication **(To be implemented within Part 3)**  
- Modern **Material 3 UI** design.  

### Node.js Backend
- RESTful API built with **Express**.  
- Authentication with **JWT** & **Google OAuth2**.  
- MongoDB for persistent storage.  
- Secure password hashing with **bcrypt**.  
- Error handling & logging middleware.

---

## 🏗️ Architecture
### Android
- **MVVM Architecture**  
- **Retrofit + OkHttp** for API calls  
- **Glide** for image loading  
- **Room** for local storage  **(To be implemented in Part 3)**  
- **Material Components** for UI  

### Backend
- **Express.js** app with modular routes & controllers  
- **Native MongoDB**  
- **JWT authentication middleware**  
- **Google OAuth2 client** for mobile sign-in
- **Cloudinary Config** for image and file upload storage

---

## ⚙️ Installation & Setup
### Clone the repository and Run
git clone --branch main https://github.com/VCSTDN2024/prog7314-part2-grad_iq.git  
Sync gradle files  
Run the app  

## Demo Links  
TBD
