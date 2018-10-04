const mongoose = require('mongoose');

module.exports = function () {
    const db = "mongodb://ali:12345678a@ds145412.mlab.com:45412/arenar";
    //const db = "mongodb://localhost/othello";
    mongoose.connect(db)
        .then(() => console.log(`Connected to ${db}...`));
    try {
        //todo
    }catch (e) {
        
    }
}