# DateCalc a Date Calculator by Eyal Ilsar

A command line executable which reads two (2) date string representations in the form YYYY-MM-DD
of dates in the supported range 1901-01-01 to 2999-12-31 and computes the number of full days between them.

## Prerequisites:
Java 1.8 toolchain (JDK & JRE) installed and tools javac and java are in the path

## Compile with:
 \> javac DateCalc.java

## Run with:
 \> java DateCalc \<fromDateString\> \<tillDateString\>

## Example run:
 \> java Main 1983-06-02 1983-06-22

1983-06-02 - 1983-06-22: 19 days

## Assumptions about the input:
 1. Provided dates will be in chronological order so till date will be same or later than from date
 2. Provided dates will be in the format YYYY-MM-DD
 3. Provided dates are valid dates

## Implementation guidelines/decisions
 1. Declare all the Date related constants within an encapsulating data holding Date class
 2. Expose all the Date logic that deals with string parsing and validation of date part ranges and limits as
    static utility functions of the Date class
 3. Keep the computation of the days difference external to the Date class as it is a business logic
 4. Dates which are outside of the supported range are clipped to the closest extreme date

 ## Comments
 1. Note that another solution would have been to calculate the days offset for each given date from the starting
 minimum date 1900-01-01 and then subtract these new date representations. This might have been simpler and neater
 but less efficient since we would do twice as many loops unless we run them in parallel!
 2. I was unable to get the third sample test case "989-01-03 - 1983-08-03: 1979 days" to work but as mentioned
 above in decison 4, I made a conscious decision to clip dates outside of the specified supported range so the
 answer my solution gives is "989-01-03 - 1983-08-03: 30226 days" as it computes the full days difference between
 1901-01-01 and 1983-08-03.
