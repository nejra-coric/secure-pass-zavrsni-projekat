import { Response, Router } from "express";
import { PasswordEntry } from "../models/PasswordEntry";
import { AuthRequest, authMiddleware } from "../middleware/auth";
import { decrypt, encrypt } from "../utils/encryption";

const router = Router();

router.use(authMiddleware);

router.get("/", async (req: AuthRequest, res: Response) => {
  try {
    const entries = await PasswordEntry.find({ userId: req.userId }).sort({
      createdAt: -1,
    });

    const passwords = entries.map((entry) => ({
      id: entry._id,
      title: entry.title,
      username: entry.username,
      password: decrypt(entry.encryptedPassword),
      url: entry.url,
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
    }));

    res.json({ passwords });
  } catch (error) {
    console.error("Get passwords error:", error);
    res.status(500).json({ message: "Greška pri dohvatanju lozinki" });
  }
});

router.post("/", async (req: AuthRequest, res: Response) => {
  try {
    const { title, username, password, url } = req.body;

    if (!title || !password) {
      res.status(400).json({ message: "Naslov i lozinka su obavezni" });
      return;
    }

    const entry = await PasswordEntry.create({
      userId: req.userId,
      title,
      username,
      encryptedPassword: encrypt(password),
      url,
    });

    res.status(201).json({
      message: "Lozinka uspješno sačuvana",
      password: {
        id: entry._id,
        title: entry.title,
        username: entry.username,
        password,
        url: entry.url,
        createdAt: entry.createdAt,
        updatedAt: entry.updatedAt,
      },
    });
  } catch (error) {
    console.error("Create password error:", error);
    res.status(500).json({ message: "Greška pri čuvanju lozinke" });
  }
});

router.delete("/:id", async (req: AuthRequest, res: Response) => {
  try {
    const entry = await PasswordEntry.findOneAndDelete({
      _id: req.params.id,
      userId: req.userId,
    });

    if (!entry) {
      res.status(404).json({ message: "Lozinka nije pronađena" });
      return;
    }

    res.json({ message: "Lozinka uspješno obrisana" });
  } catch (error) {
    console.error("Delete password error:", error);
    res.status(500).json({ message: "Greška pri brisanju lozinke" });
  }
});

export default router;
