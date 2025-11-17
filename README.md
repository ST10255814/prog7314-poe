# Grad IQ – PROG7314 POE

**Repository:** `prog7314-part2-grad_iq`  
**Tech Stack:** Kotlin (Android), Node.js (Express, MongoDB), Render for hosting the RESTful API  
**Author(s):** 
- Jayden Larkins
- Gerhard Lemmer
- Thatho Mokoena
- Mokran Ait Amara

**Date:** 17 November 2025 

---

## 🚀 Release Notes – Version 1.0.0
This release brings updates and improvements to the Grad IQ application. Please see below for prerequisites, an overview of the system, and technical architecture.  

### 📋 Prerequisites  
- Android Studio Meerkat +
- SHA-1 certificate for Google SSO
- Biometrics Enrollement
- AGP of 8.10.1

---

### 🆕 New & Updated Features   
- Multilingual Support - RentWise now supports up to 4 languages which includes English, Afrikaans, Zulu and Sutho. 
- AI Chatbot for user queries - A built-in assistant that helps users quickly get answers, guidance, and support. **(User defined 4)**
- Biometric Authentication -  Fast and secure login using biometric login with support for various API device levels. **(User Defined 5)**

---

### 📌 Overview
RentWise is an application built for the PROG7314 POE.  
It consists of:

- 📱 **Android (Kotlin)** app for users to interact with the system.  
- 🌐 **Node.js (Express + MongoDB)** backend API for data management, authentication, authorisation, and CRUD functionality.  

The system enables users to **register, log in (JWT/Google Sign-In/Biometric), and perform CRUD functionality for property management** once online.  

> ⚠️ Note: The RESTful API code is on the [master branch](https://github.com/VCSTDN2024/prog7314-part2-grad_iq/tree/master).

---

### ✨ Existing Features

#### Android (Kotlin)
- User authentication (JWT / Google Sign-In)
- Google SSO
- Upload of Digital Documents **(User Defined 1)**
- Feedback and Listings **(User Defined 2)**  
- Filtering Listing with Wishlist **(User Defined 3)**
- User Setting Configuration (**Profile and App settings**)  
- Property Management CRUD (**View Listings, Maintenance requests and tracking, etc**)  
- Modern Material 3 UI design  

#### Node.js Backend
- RESTful API built with Express  
- Authentication with JWT & Google OAuth2  
- MongoDB for persistent storage  
- Secure password hashing with bcrypt  
- Error handling & logging middleware  

---

### 🏗️ Architecture
#### Android
- MVVM Architecture  
- Retrofit + OkHttp for API calls  
- Glide for image loading  
- Room for local storage (**To be implemented in Part 3**)  
- Material Components for UI  

#### Backend
- Express.js app with modular routes & controllers  
- Native MongoDB  
- JWT authentication middleware  
- Google OAuth2 client for mobile sign-in  
- Cloudinary config for image and file upload storage  

---

### ⚙️ Installation & Setup
#### Clone the repository and run:
- `git clone --branch main https://github.com/VCSTDN2024/prog7314-part2-grad_iq.git`  
- Sync gradle files  
- Run the app  

---

### 📦 Download the Android APK
#### 🔗 Download Builds
> [Download latest APKs here](https://github.com/VCSTDN2024/prog7314-part2-grad_iq/actions/workflows/generate-apk-aab-debug-release.yml)
1. Click the link above.  
2. Select the latest workflow run.  
3. Scroll down to **Artifacts**.  
4. Click **Download** for APK or AAB.  

---

### 📺 Demo Links  
TBD
