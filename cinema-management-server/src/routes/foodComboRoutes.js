// src/routes/foodComboRoutes.js

const express = require("express");
const router = express.Router();
const foodComboController = require("../controllers/foodComboController");

// GET /api/combos/cinema/:cinemaId
router.get("/cinema/:cinemaId", foodComboController.getCombosByCinema);

module.exports = router;