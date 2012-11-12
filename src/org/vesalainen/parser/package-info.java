/**
 * Provides classes for parser compiler.
 * 
 * <p>
 * 
 * The grammar is constructed with terminals and rules using annotations @Terminal and @Rule.
 * Terminal
 * 
 * Terminal is a named limited regular expression.
 * 
 * <code>
 * @Terminal(expression = "[0-9]+")
 * public int digit(String s)
 * {
 *     return Integer.parseInt(s);
 * }
 * </code>
 * The name default is the method name. In this example “digit?. In this example when regular expression ‘[0-9]+’ is used to parse the input stream, the accepted string is converted into integer by calling method digit.
 * 
 * It is also possible to convert types automatically by using abstract method. Like:
 * 
 * <code>
 * @Terminal(expression = "[0-9]+")
 * public abstract int digit();
 * </code>
 * 
 * Another way to introduce a terminal is to use the same annotation with class.
 * 
 * <code>
 * @Terminal(left="OR", expression="'|'")
 * </code>
 * 
 * You can use almost all types of regular expressions. However groups and back references are not supported.
 * 
 * Regular expression has two other limitations:
 * 
 * It cannot accept empty string like a*
 * 
 * When parser is expecting more than one terminal, it makes a new regular expression from choices. This new regular expression cannot be ambiguous. It must accept only one terminal. Example of ambiguous combination is:
 * 
 * <code>
 * [a-c]+a and [b-d]+a with a string “bcbca? you cannot decide which one to use
 * </code>
 * 
 * 
 * Both of these errors are reported during compilation.
 * 
 * Several terminals can be grouped with @Terminals annotation. Like:
 * 
 * <code>
 * @Terminals({
 *     @Terminal(left="SYMBOL", expression="'[a-z]+'"),
 *     @Terminal(left="PRODUCES", expression="'::='"),
 *     @Terminal(left="OR", expression="'|'")
 * })
 * </code>
 * 
 * Rule
 * 
 * Rule is added by using @Rule annotation. It is used with class or method.
 * 
 * When used with method, the method is a reducer for the rule. When rule right hand side is accepted the rule method is called.
 * 
 * Rule method left side type must be the same as methods return type.
 * 
 * methods arguments must match with right hand side non void symbols. Example:
 * 
 * 
 * <code>
 * @Rule({"braceStart", "digit", "comma", "digit", "braceEnd"})
 * public Quantifier braceQ2(int min, int max)
 * {
 *     return new Quantifier(min, max);
 * }
 * </code>
 * 
 * In this example braceStart, comma and braceEnd are terminals without a method. Digit is a terminal with method returning int.
 * 
 * Note that automatic conversions can happen here too. This means that digit could also return double. (or something else that could be converted to in
 * 
 *  */
package org.vesalainen.parser;
