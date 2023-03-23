GET http://localhost:8080/stats

HTTP/* *

[Asserts]
duration < 200
status == 200

jsonpath "$.users"  == 3003842
body contains "changesets"
body contains "users"
body contains "roads"
body contains "buildings"
body contains "edits"
body contains "latest"
body contains "hashtag"
