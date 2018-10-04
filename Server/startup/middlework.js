const {User} = require('../models/user');
module.exports =  async (req, res, next) => {
    if (req.body._id){
        if(req.body._id.toString().trim()==""){
          return  res.state(400).send("_id error");
        }
        try {
            await User.findByIdAndUpdate(req.body._id, {
                $set:
                    {
                        date: new Date()
                    }
            });
        }catch (e) {
            console.log(e);
        }
    }
    next()
};
