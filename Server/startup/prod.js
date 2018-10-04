const helmet=require('helmet');
const compression=require('compression');
const {Onlines} = require('../models/onlines');
module.exports= function (app) {
  app.use(helmet());
  app.use(compression());
    async function start(){
        await Onlines.find({},async function(err, doc) {
            if(err || doc.length === 0) {
              var db= await new Onlines();
              await db.save();
              console.log("creat db..")
            }
        });
    }
    start();
};