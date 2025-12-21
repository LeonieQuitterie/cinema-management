// src/controllers/commentController.js
const CommentService = require("../services/commentService");

class CommentController {
  static async postComment(req, res) {
    try {
      const { movieId } = req.params;
      const { content, hasSpoiler, parentId } = req.body;

      const userId = req.user.id;

      if (!content || content.trim() === "") {
        return res.status(400).json({
          success: false,
          message: "Nội dung bình luận không được để trống"
        });
      }

      const comment = await CommentService.createComment({
        movieId,
        userId,
        content,
        hasSpoiler,
        parentId
      });

      return res.status(201).json({
        success: true,
        message: "Đăng bình luận thành công",
        data: comment
      });

    } catch (err) {
      console.error("POST COMMENT ERROR:", err);
      return res.status(500).json({
        success: false,
        message: "Lỗi server"
      });
    }
  }
}

module.exports = CommentController;
