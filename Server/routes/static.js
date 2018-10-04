const express = require('express');
const router = express.Router();
router.get('/test', function(req, res){
  res.sendFile("/static/image/test.png");
});
module.exports = router;
