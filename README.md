##  HealthData Retrieval

All data retrieval functions must be called from the **singleton**:

- `HealthConnectManager.getInstance(context)` → if you have a **linked device**  
- `FakeHealthConnectManager.getInstance()` → if you want to use **random data**

---

###  APIs

```text
• readSteps(startTime, endTime)
• readHeartRate(startTime, endTime)
• readActiveCaloriesBurned(startTime, endTime)
• readBloodGlucose(startTime, endTime)
• readBloodPressure(startTime, endTime)
• readBodyFat(startTime, endTime)
• readBodyTemperature(startTime, endTime)
• readBodyWaterMass(startTime, endTime)
• readHydration(startTime, endTime)
• readNutrition(startTime, endTime)
• readOxygenSaturation(startTime, endTime)
• readRespiratoryRate(startTime, endTime)
• readSleepSessions(startTime, endTime)
• readTotalCaloriesBurned(startTime, endTime)
