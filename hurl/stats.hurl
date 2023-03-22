GET http://localhost:8080/stats

HTTP/* *

[Asserts]
jsonpath "$.users"  == 3003842
duration < 400
body contains "changesets"
body contains "users"
body contains "roads"
body contains "buildings"
body contains "edits"
body contains "latest"
body contains "hashtag"
