@@
     public EmployeeSchedule generateDemoData(DemoDataParameters parameters) {
         EmployeeSchedule employeeSchedule = new EmployeeSchedule();
@@
-        employeeSchedule.setShifts(shifts);
+        employeeSchedule.setShifts(shifts);
+
+        // Example: create an empty mustWorkTogetherList by default so API clients see the field.
+        employeeSchedule.setMustWorkTogetherList(java.util.Collections.emptyList());
 
         return employeeSchedule;
     }
@@
 }
