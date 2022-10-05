# forex

Solution for the task described in `Forex.md`.

Implementation assumes service is running on a system with correctly configured clock and is using system clock to determine whether rate fetched from `OneFrame` service is up to date or not.

Few implementation details

- Proxy service is using in-memory cache that is updated with configurable duration
- Given small amount of currency pairs service is fetching all possible pairs (cartesian product) in single request using `/rate` api
- Cache update is running in separate thread and using `Ref` to share current the last updated value
- In case rate for currency pair is outdated or not found in cache corresponding error type is returned (no fallback calling logic is implemented atm)

Assuming OneFrame service is running.

```bash
docker run -p 8080:8080 paidyinc/one-frame
```

To run

```bash
export ONE_FRAME_TOKEN=10dc303535874aeccc86a8251e6992f5
sbt run
```

This would start server on port specified in config file.

To query

```bash
curl 'http://localhost:5000/rates?from=USD&to=SGD' | jq .
```

To run tests

```bash
sbt testOnly 'forex.*'
```

To run integration test

```bash
sbt testOnly 'integration.*'
```
