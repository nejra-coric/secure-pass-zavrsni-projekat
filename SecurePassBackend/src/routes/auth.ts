import bcrypt from "bcryptjs";
import { Request, Response, Router } from "express";
import { User } from "../models/User";
import { signToken } from "../utils/jwt";

const router = Router();

router.post("/register", async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      res.status(400).json({ message: "Email i lozinka su obavezni" });
      return;
    }

    if (password.length < 8) {
      res.status(400).json({ message: "Lozinka mora imati najmanje 8 karaktera" });
      return;
    }

    const existingUser = await User.findOne({ email: email.toLowerCase() });

    if (existingUser) {
      res.status(409).json({ message: "Korisnik sa ovim emailom već postoji" });
      return;
    }

    const hashedPassword = await bcrypt.hash(password, 12);
    const user = await User.create({
      email: email.toLowerCase(),
      password: hashedPassword,
    });

    res.status(201).json({
      message: "Registracija uspješna",
      user: {
        id: user._id,
        email: user.email,
      },
    });
  } catch (error) {
    console.error("Register error:", error);
    res.status(500).json({ message: "Greška pri registraciji" });
  }
});

router.post("/login", async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      res.status(400).json({ message: "Email i lozinka su obavezni" });
      return;
    }

    const user = await User.findOne({ email: email.toLowerCase() });

    if (!user) {
      res.status(401).json({ message: "Neispravan email ili lozinka" });
      return;
    }

    const isValidPassword = await bcrypt.compare(password, user.password);

    if (!isValidPassword) {
      res.status(401).json({ message: "Neispravan email ili lozinka" });
      return;
    }

    const token = signToken(user._id.toString());

    res.json({
      token,
      user: {
        id: user._id,
        email: user.email,
      },
    });
  } catch (error) {
    console.error("Login error:", error);
    res.status(500).json({ message: "Greška pri prijavi" });
  }
});

export default router;
