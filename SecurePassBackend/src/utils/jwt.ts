import jwt from "jsonwebtoken";

export interface JwtPayload {
  userId: string;
}

export function signToken(userId: string): string {
  const secret = process.env.JWT_SECRET;

  if (!secret) {
    throw new Error("JWT_SECRET nije definisan u .env fajlu");
  }

  return jwt.sign({ userId }, secret, { expiresIn: "7d" });
}

export function verifyToken(token: string): JwtPayload {
  const secret = process.env.JWT_SECRET;

  if (!secret) {
    throw new Error("JWT_SECRET nije definisan u .env fajlu");
  }

  return jwt.verify(token, secret) as JwtPayload;
}
