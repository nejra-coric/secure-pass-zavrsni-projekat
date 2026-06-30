import mongoose, { Document, Schema, Types } from "mongoose";

export interface IPasswordEntry extends Document {
  userId: Types.ObjectId;
  title: string;
  username?: string;
  encryptedPassword: string;
  url?: string;
  createdAt: Date;
  updatedAt: Date;
}

const passwordEntrySchema = new Schema<IPasswordEntry>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    title: {
      type: String,
      required: true,
      trim: true,
    },
    username: {
      type: String,
      trim: true,
    },
    encryptedPassword: {
      type: String,
      required: true,
    },
    url: {
      type: String,
      trim: true,
    },
  },
  { timestamps: true }
);

export const PasswordEntry = mongoose.model<IPasswordEntry>(
  "PasswordEntry",
  passwordEntrySchema
);
