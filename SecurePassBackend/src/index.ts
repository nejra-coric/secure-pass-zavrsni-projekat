import cors from "cors";
import dotenv from "dotenv";
import express from "express";
import { connectDB } from "./config/db";
import authRoutes from "./routes/auth";
import passwordRoutes from "./routes/passwords";

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

app.get("/health", (_req, res) => {
  res.json({ status: "ok" });
});

app.use("/api/auth", authRoutes);
app.use("/api/passwords", passwordRoutes);

async function start() {
  try {
    await connectDB();
    app.listen(port, () => {
      console.log(`Server pokrenut na portu ${port}`);
    });
  } catch (error) {
    console.error("Greška pri pokretanju servera:", error);
    process.exit(1);
  }
}

start();
