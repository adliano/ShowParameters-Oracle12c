/*
 Author: Adriano Alves
 Date  : 09/30/16
 Program Name: ShowParameters.java
 Objective: HW2 CS151B
            program to show the availables and undocmented parameters
            of Oracle database 12C.
 Available options: -n : display count of parameters
                    -u : display undocumented parameters
*/
 
import java.util.*;
import java.util.stream.*;
import java.util.regex.*;
 
class ShowParameters
{
    private static String displayPrompt=null;

    public static void main(String args[])
    {
        parseArgs(args);
    }
    ///////////////////// parse Args ////////////////////////////
    // this method will get the argumentes from command line and will 
    // parse the options and the regexp
    private static void parseArgs(String arguments[])
    {
        switch(arguments.length)
        {
            case 0: // if no arguments show all regular parameters
                showParameters();
            break;

            case 1: // if only one argumet check for option or regexp
                if(!arguments[0].contains("-") && isValidRegexp(arguments[0])) 
                {
                    showParameters(arguments[0]);
                }
                else // if contains "-" means is not a option
                {
                    switch(arguments[0])
                    {
                        // display count of parameters
                        case "-n":
                                countParam();
                        break;
                        // display all undocmented parameters
                        case "-u":
                                showUndocParam();
                        break;
                        // display count of undocumented parameters
                        case "-un":
                                countUndocParam();
                        break;
                        // display count of undocumented parameters
                        case "-nu":
                                countUndocParam();
                        break;
                        default: // inlvalid option
                                die(null);
                        break;
                    }
                }
            break;

            case 2: // if 2 argumentes check for option + regexp
                switch(arguments[0])
                {
                   //display count of parameters of given regexp
                   case "-n":
                           countParam(arguments[1]);
                   break;
                   //display undocmented parameters of given regexp
                   case "-u":
                           showUndocParam(arguments[1]);
                   break;
                   //display count of undocumented parameters of given regexp
                   case "-un":
                           countUndocParam(arguments[1]);
                   break;
                   //display count of undocumented parameters of given regexp
                   case "-nu":
                           countUndocParam(arguments[1]);
                   break;
                   default: // something wrog happen :-(
                           die(null);
                   break;
                }
                
            break;
        }
    }
    /////////////// count parameters ////////////////
    private static void countParam(String...rgx)
    {
        String temp = "select count(name) from V$PARAMETER";
        String query = rgx.length <= 0 ? temp : temp+
                    " where regexp_like(name,'"+rgx[0]+"')";
        String withRegexp = rgx.length >0 ?"With Regexp("+rgx[0]+")" : "";
        Oracle ora = new Oracle("sys");
        String results[] = ora.doSql(query);
        if(ora.sysError) for(String s : results) die(s);
        else println(String.format("Total of Parameters %s:\t%s",
                                    withRegexp,results[0]));
    }
    ////////////// display Parameters /////////////////
    private static void showParameters(String...rgx)
    {
        String temp = rgx.length > 0 ? " where regexp_like(name,'"
                                        +rgx[0]+"')" : "";
        String query = String.format("select name,value from V$PARAMETER %s "+
                                     "order by name",temp);
        Oracle ora = new Oracle("sys");
        List<String> results = Arrays.asList(ora.doSql(query));
        if(ora.sysError) results.forEach(ShowParameters::println);
        else results.stream().forEach(ShowParameters::println);
        //TODO try display key:value format at the same line
    }
    //////// display undocmented parameter ////////////
    private static void showUndocParam(String...regexp)
    {
        String temp = regexp.length > 0 ? " and regexp_like(ksppinm,'"+
                                           regexp[0]+"')" : "";
        String query = String.format("select a.ksppinm parameter,"+
                "b.ksppstvl instance_value from x$ksppi a, x$ksppcv b"+
                " where a.indx = b.indx and "+
                "substr(a.ksppinm,1,1) = '_' %s order by a.ksppinm",temp);
        Oracle ora = new Oracle("sys");
        List<String> results = Arrays.asList(ora.doSql(query));
        if(ora.sysError) results.forEach(ShowParameters::println);
        else results.stream().forEach(ShowParameters::println);
        //TODO try display key:value format at the same line
    }
    ////////////// count undocmented parameter /////////////
    private static void countUndocParam(String...regexp)
    {
        String temp = regexp.length >0 ? " and regexp_like(ksppinm,'"+
                                         regexp[0]+"')" : "";
        String query = String.format("select count(ksppinm) from x$ksppi "+
                "where substr(ksppinm,1,1) = '_' %s ",temp);
        String withRegexp = regexp.length >0 ?"With Regexp("+regexp[0]+")" : "";
        Oracle ora = new Oracle("sys");
        String results[] = ora.doSql(query);
        if(ora.sysError) for(String s : results) die(s);
        else println(String.format("Total of Parameters %s:\t%s",
                    withRegexp,results[0]));
    }
    /////////////// isValidRegexp ////////////////
    // method to check if the regexp is valid 
    private static boolean isValidRegexp(String regexp)
    {
        try
        {
            Pattern.compile(regexp);
        }
        catch(PatternSyntaxException e)
        {
            die("Invalid Regexp");
        }
        return true;
    }
    ////////////// die //////////////////
    // method to display error msg , if null argument it will display usage msg
    private static void die(String msg)
    {
        Optional<String> opt = Optional.ofNullable(msg);
        String usage = "usage:java ShowParameters <-n | -u> <regexp>";
        System.err.println(opt.orElse(usage));
        System.exit(1);
    }
    /////////////// println ///////////////
    private static void println(Object o)
    {
        System.out.println(String.valueOf(o));
    }
}
