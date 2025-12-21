// src/routes/commentRoutes.js
const express = require("express");
const CommentController = require("../controllers/commentController");
const authMiddleware = require("../middlewares/authMiddleware");

const router = express.Router();

router.post(
  "/comments/:movieId",
  authMiddleware,
  CommentController.postComment
);

module.exports = router;
