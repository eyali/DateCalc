/*
 * Date Calculator by Eyal Ilsar
 *
 * A command line executable which reads two (2) date string representations in the form YYYY-MM-DD
 * and computes the number of full days between them.
 *
 * Prerequisites:
 * Java 1.8 toolchain (JDK & JRE) installed and tools javac and java are in the path
 *
 * Compile with:
 * > javac DateCalc.java
 *
 * Run with:
 * > java DateCalc <fromDateString> <tillDateString>
 *
 * Example run:
 * > java Main 1983-06-02 1983-06-22
 * 1983-06-02 - 1983-06-22: 19 days
 *
 * Assumptions about the input:
 * 1. Provided dates will be in chronological order so till date will be same or later than from date
 * 2. Provided dates will be in the format YYYY-MM-DD
 * 3. Provided dates are valid dates
 *
 * Implementation guidelines/decisions
 * 1. Declare all the Date related constants within an encapsulating data holding Date class
 * 2. Expose all the Date logic that deals with string parsing and validation of date part ranges and limits as
 *    static utility functions of the Date class
 * 3. Keep the computation of the days difference external to the Date class as it is a business logic
 * 4. Dates which are outside of the supported range are clipped to the closest extreme date
 *
 * Note that another solution would have been to calculate the days offset for each given date from the starting
 * minimum date 1900-01-01 and then subtract these new date representations. This might have been simpler and neater
 * but less efficient since we would do twice as many loops unless we run them in parallel!
 */
public class DateCalc {
    // Encapsulate a Date class with date parts parsing from a String representation
    // and logic for validation (clipping) into supported date range.
    public static class Date {
        // Constant number of days in a regular year
        static final int NUM_OF_DAYS_IN_A_REGULAR_YEAR = 365;

        // Constant number of days in a leap year
        static final int NUM_OF_DAYS_IN_A_LEAP_YEAR = NUM_OF_DAYS_IN_A_REGULAR_YEAR + 1;

        // 0-indexed (January=0) list of number of days per month for fast lookup
        static final int MONTHS_LENS[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        // Returns whether a given year is a leap year
        static boolean isLeapYear(int year) {
            return (year % 400 == 0) || ((year % 4 == 0) || (year % 100 != 0));
        }

        // Returns the number of days in a particular month in a specific year
        static int getDaysInMonth(int month, int year) {
            return MONTHS_LENS[month - 1] + ((month == 2) && isLeapYear(year) ? 1 : 0);
        }

        // Returns number of days in a particular year
        static int getDaysInYear(int year) {
            return (Date.isLeapYear(year) ? Date.NUM_OF_DAYS_IN_A_LEAP_YEAR : Date.NUM_OF_DAYS_IN_A_REGULAR_YEAR);
        }

        // Extreme applicable dates
        static final Date MIN_DATE = new Date(1901, 1, 1);
        static final Date MAX_DATE = new Date(2999, 12, 31);

        // Date parts
        int year;
        int month;
        int day;

        // Internal constructor for constant instances
        private Date(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        // External constructor from date String representation
        public Date(String dateString) {
            String dateParts[] = dateString.split("-");

            year = Integer.parseUnsignedInt(dateParts[0]);
            month = Integer.parseUnsignedInt(dateParts[1]);
            day = Integer.parseUnsignedInt(dateParts[2]);

            // Clip date to minimum
            if ((year < MIN_DATE.year) ||
                    ((year == MIN_DATE.year) && (month < MIN_DATE.month)) ||
                    ((year == MIN_DATE.year) && (month == MIN_DATE.month) && (day < MIN_DATE.day))
            ) {
                year = MIN_DATE.year;
                month = MIN_DATE.month;
                day = MIN_DATE.day;
            }

            // Clip date to maximum
            if ((year > MAX_DATE.year) ||
                    ((year == MAX_DATE.year) && (month > MAX_DATE.month)) ||
                    ((year == MAX_DATE.year) && (month == MAX_DATE.month) && (day > MAX_DATE.day))
            ) {
                year = MAX_DATE.year;
                month = MAX_DATE.month;
                day = MAX_DATE.day;
            }
        }
    }

    // Compute the number of full days difference between two given dates yf-mf-df and yt-mt-dt
    //
    // Corner cases:
    // Same date
    //        df == dt        mf == mt (yf == yt)
    // |------|---------------|
    // dd = dt - df = 0
    //
    // Consecutive dates
    //        df   dt         mf == mt (yf == yt)
    // |------()----------|
    // dd = dt - df - 1 = 0
    //
    // Dates within the same month and year
    //        df   dt         mf == mt (yf == yt)
    // |------(----)----------|
    // dd = dt - df - 1
    //
    // Dates within consecutive months in the same year
    //                   df   mf   dt        mt (mt - mf = 1) (yf == yt)
    // |-----------------(----+----)---------|
    // dd = mf - df + dt - 1 = (dt - df) + mf - 1
    //
    // Dates in months with one month apart in the same year
    //                   df   mf             m1    dt         mt (mt - mf = 2) (yf == yt)
    // |-----------------(----+--------------+-----)----------|
    // dd = mf - df + m1 + dt - 1 = (dt - df) + mf + m1 - 1
    //
    // Dates within multiple months apart in the same year
    //                   df   mf             m1               m2  dt         mt (mt - mf > 2) (yf == yt)
    // |-----------------(----+--------------+----------------+---)----------|
    // dd = mf - df + m1 + dt - 1 = (dt - df) + mf + m1 + m2 - 1 = (dt - df) + {mf +...+ mt(-1))@yf - 1
    //
    // Dates within multiple months apart in consecutive years
    //                   df   mf@yf          m12@yf           m1@yt        mt(-1)@yt dt     mt@yt (mt - mf > 2) (yt - yf > 1)
    // |-----------------(----+-----...------+----------------+----...-----|---------}------|
    // dd = mf@yf - df + mf(+1)...m12@yf + m1@yt..mt(-1)@yt + dt - 1 = (dt - df) + (mf +...+ m12)@yf + (m1 +...+ mt(-1))@yt - 1
    //
    // Dates multiple years apart
    //                   df   mf@yf          m12@yf    m1@yf(+1)  m12@yt(-1)  m1@yt      mt(-1)@yt dt       mt@yt (yt - yf > 2)
    // |-----------------(----+-----...------+----...--+----...---+-----------+----...---|---------)--------|
    // dd = mf@yf - df + mf(+1)...m12@yf + yf(+1) + yt(-1) + m1@yt..mt(-1)@yt + dt - 1 = (dt - df) + (mf +...+ m12)@yf + (m1 +...+ mt(-1))@yt + yf(+1) + ... + yt(-1) - 1
    //
    static int getNumberOfFullDaysDifference(Date from, Date till) {
        int numOfFullDaysDifference = 0;

        // Calculate each of the date parts difference
        int numOfDays = till.day - from.day;
        int numOfMonths = till.month - from.month;
        int numOfYears = till.year - from.year;

        // Firstly, assume day parts are within the same year and month
        // so we add the day difference between the from date and the till date
        numOfFullDaysDifference = numOfDays;

        // Secondly, add the month parts difference
        // Add the days in the months from to till exclusive within the from year
        if (numOfMonths > 0) { // If till month is greater than from month
            // Add the days of the months in from year
            for (int month = from.month; month < till.month; month++) {
                numOfFullDaysDifference += Date.getDaysInMonth(month, from.year);
            }
        } else if (numOfMonths < 0) { // If till month is less than from month need to
            // add the months in the from year (till including December) and
            // the months in the till year (from January not including the till month) and
            // remove a year from the year difference

            // Add the days of the months in the from year
            for (int month = from.month; month <= 12; month++) {
                numOfFullDaysDifference += Date.getDaysInMonth(month, from.year);
            }
            // Add the days of the months in the till year
            numOfYears--;
            for (int month = 1; month < till.month; month++) {
                numOfFullDaysDifference += Date.getDaysInMonth(month, till.year);
            }
        }

        // Thirdly, add the year parts difference
        // Add the days in the years from to till exclusive
        if (numOfYears > 0) {
            for (int year = from.year; year < till.year; year++) {
                numOfFullDaysDifference += Date.getDaysInYear(year);
                // BTW, I originally accumulated the span of 12 months commented below
                // but decided it is quicker to simply lookup the total days in each year as above
                //  for (int month = 1; month <= 12; month++) {
                //      numOfFullDaysDifference += getDaysInMonth(month, year);
                //  }
            }
        }

        // Finally, unless the dates are the same we need to remove a day to accommodate for the two partial from and till dates
        if (!(numOfDays == 0 && numOfMonths == 0 && numOfYears == 0)) {
            numOfFullDaysDifference--;
        }

        return numOfFullDaysDifference;
    }

    public static void main(String[] args) {
        // Read from and till date strings from command line
        if (args.length < 2) {
            // Tell user how to use the application since mandatory arguments were missing
            System.out.println("Usage: java DateCalc <fromDate> <tillDate>\n" +
                    "Computes the number of full days between given dates in the range 1901-01-01 to 2999-12-31\n\n" +
                    "Mandatory arguments:\n" +
                    "fromDate\tperiod staring date in the format YYYY-MM-DD\n" +
                    "tillDate\tperiod ending date in the format YYYY-MM-DD");
        } else {
            String fromDateString = args[0];
            String tillDateString = args[1];

            // Create from and till Date instances
            Date from = new Date(args[0]);
            Date till = new Date(args[1]);

            // Print to console computed number of full days difference between from and till dates
            int fullDaysDiff = getNumberOfFullDaysDifference(from, till);
            System.out.println(fromDateString + " - " + tillDateString + ": " + fullDaysDiff + " day" + (fullDaysDiff == 1 ? "" : "s"));
        }
    }
}
