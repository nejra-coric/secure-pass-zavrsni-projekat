import mongoose from "mongoose";

export async function connectDB(): Promise<void> {
  const uri = process.env.MONGODB_URI;

  if (!uri) {
    throw new Error("MONGODB_URI nije definisan u .env fajlu");
  }

  await mongoose.connect(uri);
  console.log("MongoDB povezan");
}
