from datetime import datetime as dt
from statistics import mean

import pandas as pd
import requests

url = "http://localhost:8080"

tests = {
    "stats": [{
        "endpoint": "/stats",
        "method": "GET",
        "headers": {
            "hashtag": "missingmaps"
        }
    }],
    "statsInterval": [{
        "endpoint": "/stats/interval",
        "method": "GET",
        "headers": {
            "hashtag": "missingmaps",
            "interval": "P1M"
        }
    }],
    "statsCountry": [{
        "endpoint": "/stats/country",
        "method": "GET",
        "headers": {
            "hashtag": "missingmaps"
        }
    }],
    "mostUsedHashtags": [{
        "endpoint": "/mostUsedHashtags",
        "method": "GET",
        "headers": {}
    }],
    "metadata": [{
        "endpoint": "/metadata",
        "method": "GET",
        "headers": {}
    }]
}

limit = 5  # Number of requests to send per test parameter
data = []

for test_name, test_parameters in tests.items():
    for test_parameter_it in test_parameters:
        for noCache in [True, False]:
            headers = test_parameter_it["headers"]
            headers["noCache"] = str(noCache).lower()
            if not noCache:
                res = requests.get(url + test_parameter_it["endpoint"], headers=headers)  # Warm up cache
            results = []
            for _ in range(limit):
                res = requests.get(url + test_parameter_it["endpoint"], headers=headers)
                results.append(res.elapsed.total_seconds() * 1000)  # Convert to milliseconds
            data.append({
                "test_name": test_name,
                "test_parameters": test_parameter_it,
                "noCache": noCache,
                "min": min(results),
                "max": max(results),
                "avg": mean(results)
            })

df = pd.DataFrame(data)

df.to_csv(f"../benchmark/results_{dt.now()}.csv", index=False)
