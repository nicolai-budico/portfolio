# Coding Java Challenge solution

### LAUNCH APPLICATION

1. Launch using maven

```
$ mvn spring-boot:run
```

2. Launch using Java command line
```
$ mvn clean package
$ java -jar target/profile-1.0.jar
```

### APIs

#### List of pre-defined categories

`GET  /api/categories` - Returns list of pre-defined categories <br/>
Response:
```json
[
  {
    "id": "IC1",
    "title": "Bonds"
  },
  {
    "id": "IC2",
    "title": "Large Cap"
  },
  {
    "id": "IC3",
    "title": "Mid Cap"
  },
  {
    "id": "IC4",
    "title": "Foreign"
  },
  {
    "id": "IC5",
    "title": "Small Cap"
  }
]
```

#### List of pre-defined portfolios
`GET  /api/portfolios`        - Returns list of pre-defined categories<br/>
Response:
```json
[
  {
    "level": 1,
    "allocation": [
      {
        "categoryId": "IC1",
        "toleranceScore": 80
      },
      {
        "categoryId": "IC2",
        "toleranceScore": 20
      },
      {
        "categoryId": "IC3",
        "toleranceScore": 0
      },
      {
        "categoryId": "IC4",
        "toleranceScore": 0
      },
      {
        "categoryId": "IC5",
        "toleranceScore": 0
      }
    ]
  },
  ...
]
```

#### Request recommendation

`POST /api/recommendations/`  - Submits calculation task into the queue and returns task id and status<br/>
Request:
```json
{
  "request": [
    {
      "amount": 10,
      "categoryId": "IC1",
      "toleranceScore": 45
    },
    {
      "amount": 45,
      "categoryId": "IC2",
      "toleranceScore": 55
    }
  ]
}
```
Response:
```json
{
  "taskId": "1c9824e7-f5c2-47c6-aa56-c5150f32cc58",
  "status": "RUNNING"
}
```

#### Receive task status

`GET /api/recommendations/status/{taskId}` - Receive task status<br/>
Parameters:
* taskId - Task id obtained from result of `POST /api/recommendations/`

Response:
```json
{
  "status": "COMPLETED",
  "taskId": "1c9824e7-f5c2-47c6-aa56-c5150f32cc58"
}
```

#### Receive task result

`GET /api/recommendations/result/{taskId}` - Receive task status<br/>
Parameters:
* taskId - Task id obtained from result of `POST /api/recommendations/`

Response:
```json
{
  "result": [
    {
      "categoryId": "IC1",
      "toleranceScore": 45,
      "oldAmount": 10,
      "newAmount": 24.75,
      "difference": 14.75
    },
    {
      "categoryId": "IC2",
      "toleranceScore": 55,
      "oldAmount": 45,
      "newAmount": 30.25,
      "difference": -14.75
    }
  ],
  "transfers": [
    {
      "source": "IC2",
      "destination": "IC1",
      "amount": 14.75
    }
  ]
}
```

#### Check system status

`GET /api/recommendations/stats` - Returns tasks count summary grouped by task status <br/>
Response:
```json
{
  "QUEUED": 20,
  "COMPLETED": 1018,
  "RUNNING": 2
}
``` 

### SWAGGER DOCS

After application is started Swagger documentation and sandbox are accessible by URL:
**http://localhost:8080/api/swagger-ui.html**

### CHANGE APPLICATION PARAMETERS

By default application is configured to execute maximum 2 tasks in parallel and perform delay before complete each task 
for 5000 ms.
To change these settings pass parameters in command line:

```
$ java -jar target/profile-1.0.jar --execution.max-tasks=10 --throttle-ms=0
```

or 

```
$ mvn spring-boot:run -Dexecution.max-tasks=10 -Dthrottle-ms=0
```

### MATH

Algorithm used to calculate recommendations:<br/>
Let say we have structure that describes each investment category like this:
```java
class Item {
    Double oldAmount;
    Double newAmount;
    Double difference; // newAmount - oldAmount  
}
``` 

1. Select two categories with most closest absolute values of `difference` fields, ignoring those with `difference == 0` 
2. The smaller `difference` is used as transfer amount
3. Decrease `difference` of these two categories using smaller `difference` 
4. Repeat steps 1-3 until there are categories with non-zero `difference`

*I have no mathematical proof for this algorithm.*
Here are some statement without mathematical proof: 
* Maximum number of iterations are N-1, where N - is a number of investment categories in request, for any initial 
  amounts while sum of tolerance scores across all investment categories in request is 100%
* Minimal number of iterations are 0, when funds are already "ideally" allocated
* Algorithm would work even with negative amounts (given that investment categories allow negative amounts)

### IMPLEMENTATION NOTES

1. `CategoryController` provides a list of pre-defined investment categories and a list of pre-defined "ideal" 
portfolios allocations, anyway `RecommendationController` allows to use self-defined category ids and portfolio allocation.

3. Tests are not implemented, except test for allocation algorithm

4. API `POST /api/recommendations/` doesn't return result immediately but task identificator only with task status.
     To obtain results UI should perform call to `GET /api/recommendations/result/{taskId}` to receive new funds allocation.
     This allows to not reject user's requests when there are no free execution slots to calculate transfers.

5. Assuming that all tasks are stored in the database or other storage, real database is not connected to the application. 
`TaskRepositoryImpl` only imitates work with database.
