var express = require('express');
var read = require('node-readability');

var app = express();

app.get('/get', function (req, res){
    var url = req.query.url;
    if(url == undefined || url == ""){
        res.status(422).json({error: "The page url cannot be blank."}).end();
    }
    else{
        read(url, function(err, article, meta) {
            result = {
                title: article.title,
                statusCode: meta.statusCode,
                host: meta.request.host,
                href: meta.request.href,
                content: article.content,
                html: article.html
            };

            article.close();

            res.json(result);

        });

    }
});

var server = app.listen(8000, function () {

    var host = server.address().address;
    var port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);

});
