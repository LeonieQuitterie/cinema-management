// src/services/commentService.js
const { v4: uuidv4 } = require("uuid");
const db = require("../config/database");

class CommentService {
  static async createComment({
    movieId,
    userId,
    content,
    hasSpoiler = false,
    parentId = null
  }) {
    const id = "cmt_" + uuidv4().split("-")[0];

    const sql = `
      INSERT INTO comments (id, movie_id, user_id, content, has_spoiler, parent_id)
      VALUES (?, ?, ?, ?, ?, ?)
    `;

    await db.query(sql, [
      id,
      movieId,
      userId,
      content,
      hasSpoiler,
      parentId
    ]);

    return {
      id,
      movieId,
      userId,
      content,
      hasSpoiler,
      parentId
    };
  }
}

module.exports = CommentService;
