# ScamNet — Network-Level Fraud Detection for Online Marketplaces & Job Postings

Built for BYAMN Buildathon 2026.

## Problem
Fake job listings, marketplace scams, and phishing "too good to be true" posts affect millions of people globally. Current defenses (report buttons, keyword filters) are reactive and treat every post in isolation — they don't catch coordinated scam rings that reuse photos, phone numbers, and posting patterns across multiple fake accounts.

## Solution
ScamNet is a marketplace/job platform with fraud detection built into its core. Every new listing is automatically cross-referenced against existing listings using three independent signals:

Perceptual image hashing — detects reused or visually similar product photos.
Contact reuse detection — identifies the same phone number across multiple listings.
Temporal burst detection — identifies unusual clusters of postings in short time windows when paired with another suspicious signal.

These signals feed into a union-find (disjoint set) clustering algorithm that groups connected listings into visual fraud networks, allowing ScamNet to surface coordinated suspicious activity rather than only individual flagged posts.
## Tech Stack
- **Backend:** Java, Spring Boot, Spring Data JPA
- **Database:** MySQL
- **Frontend:** React (Vite)
- **Core algorithms:** custom perceptual image hashing (dHash), union-find graph clustering

## Features
- Create marketplace or job listings with image upload
- Automatic risk scoring on every new post
- Plain-English explanations for why a post was flagged
- Fraud Network view showing connected clusters of suspicious listings
- Filter by listing type (Marketplace / Jobs)
- Dark and light mode

## AI Tool Disclosure
This project was developed with assistance from Claude (Anthropic).

Claude was used for:

Brainstorming and refining the system architecture and implementation approach
Providing implementation guidance and code suggestions
Debugging and resolving development issues
Assisting with the implementation and refinement of the fraud-detection logic and UI
Reviewing and improving parts of the project during development

I made the final decisions on the system design, algorithms, features, and implementation, and reviewed and tested the generated suggestions before incorporating them into the project.

## How to Run Locally

### Backend
1. Ensure MySQL is running, create a database named `scamnet_db`
2. Update `backend/src/main/resources/application.properties` with your MySQL credentials
3. Open the `backend` folder in Spring Tool Suite (or any IDE)
4. Run `ScamnetApplication.java`
5. Backend runs on `http://localhost:8080`

### Frontend
1. Navigate to the `frontend` folder
2. Run `npm install`
3. Run `npm run dev`
4. Frontend runs on `http://localhost:5173`

## Submitted by:
Oshin Rathore
