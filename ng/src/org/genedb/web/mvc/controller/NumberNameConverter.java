package org.genedb.web.mvc.controller;

public class NumberNameConverter {
    
    private enum Numbers {
        MILLION(1000000), THOUSAND(1000), HUNDRED(100), TEN(10);
        
        private int number;
        
        private Numbers(int number) {
            this.number = number;
        }
        
        public int getNumber() {
            return number;
        }
        
    }
    
    public static String convert(int num) {
        // FIXME Doesn't work for > 100
        switch(num) {
        case 0: return "zero";
        case 1: return "one";
        case 2: return "two";
        case 3: return "three";
        case 4: return "four";
        case 5: return "five";
        case 6: return "six";
        case 7: return "seven";
        case 8: return "eight";
        case 9: return "nine";
        case 10: return "ten";
        case 11: return "eleven";
        case 12: return "twelve";
        case 13: return "thirteen";
        case 14: return "fourteen";
        case 15: return "fifteen";
        case 16: return "sixteen";
        case 17: return "seventeen";
        case 18: return "eighteen";
        case 19: return "nineteen";
        case 20: return "twenty";
        case 30: return "thirty";
        case 40: return "fourty";
        case 50: return "fifty";
        case 60: return "sixty";
        case 70: return "seventy";
        case 80: return "eighty";
        case 90: return "ninety";
        }
        
        StringBuilder ret = new StringBuilder();
        if (num < 100) {
            // But not one of those above
            ret.append(convert(num / 10));
            ret.append("-");
            ret.append(convert(num % 10));
            return ret.toString();
        }
        

        int balance = num;
        for (Numbers number : Numbers.values()) {
            int count = balance / number.getNumber();
            if (count > 0) {
                balance -= count * number.getNumber();
                ret.append(convert(count)+" "+number.name());
            }
        }
        return ret.toString();
    }
}
