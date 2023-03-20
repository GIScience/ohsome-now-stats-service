GET http://localhost:8080/stats

HTTP/* *

[Asserts]
jsonpath "$.users"  == 3003842
duration < 400
