import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Scanner;
import java.util.logging.*;
import java.util.List;
import java.util.ArrayList;

@Retention(RetentionPolicy.RUNTIME)
@interface Loggable {}

class CalculatorLogger {
    private static final Logger LOGGER = Logger.getLogger(Calculator.class.getName());
    private static final List<String> logMessages = new ArrayList<>();

    static {
        LOGGER.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        LOGGER.addHandler(handler);
    }

    public static void log(String message) {
        LOGGER.info(message);
        logMessages.add(message);
    }

    public static void showLogs() {
        System.out.println("Registros de cálculo anteriores:");
        for (String logMessage : logMessages) {
            System.out.println(logMessage);
        }
    }
}


interface Calculator {
    @Loggable
    double add(double a, double b);

    @Loggable
    double subtract(double a, double b);

    @Loggable
    double multiply(double a, double b);

    @Loggable
    double divide(double a, double b);
}


class BasicCalculator implements Calculator {
    @Override
    public double add(double a, double b) {
        double result = a + b;
        CalculatorLogger.log("Resultado de adição " + a + " e " + b + " = " + result);
        return result;
    }

    @Override
    public double subtract(double a, double b) {
        double result = a - b;
        CalculatorLogger.log("Resultado de subtração " + a + " e " + b + " = " + result);
        return result;
    }

    @Override
    public double multiply(double a, double b) {
        double result = a * b;
        CalculatorLogger.log("Resultado de Multiplicação " + a + " e " + b + " = " + result);
        return result;
    }

    @Override
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Não Divide por Zero");
        }
        double result = a / b;
        CalculatorLogger.log("Resultado da Divisão " + a + " e " + b + " = " + result);
        return result;
    }
}


class LoggingHandler implements InvocationHandler {
    private final Object target;

    LoggingHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Loggable.class)) {
            String methodName = method.getName();
            StringBuilder argsBuilder = new StringBuilder("(");
            if (args != null && args.length > 0) {
                argsBuilder.append(args[0]);
                for (int i = 1; i < args.length; i++) {
                    argsBuilder.append(", ").append(args[i]);
                }
            }
            argsBuilder.append(")");
            String argsString = argsBuilder.toString();
            String message = methodName + argsString;
            CalculatorLogger.log("Chamando: " + message);
        }
        return method.invoke(target, args);
    }
}


class CalculatorFactory {
    static Calculator createCalculator() {
        BasicCalculator calculator = new BasicCalculator();
        InvocationHandler handler = new LoggingHandler(calculator);
        return (Calculator) Proxy.newProxyInstance(
                Calculator.class.getClassLoader(),
                new Class<?>[]{Calculator.class},
                handler
        );
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator calculator = CalculatorFactory.createCalculator();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Escolha a operação:");
            System.out.println("1. Adição");
            System.out.println("2. Subtração");
            System.out.println("3. Multiplicação");
            System.out.println("4. Divisão");
            System.out.println("5. Mostrar logs sistema");
            System.out.println("6. Sair");

            System.out.print("Escolha sua opção: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                case 2:
                case 3:
                case 4:
                    System.out.print("Primeiro Numero: ");
                    double num1 = scanner.nextDouble();
                    System.out.print("Segundo Numero: ");
                    double num2 = scanner.nextDouble();
                    performOperation(calculator, choice, num1, num2);
                    break;
                case 5:
                    CalculatorLogger.showLogs();
                    break;
                case 6:
                    System.out.println("Saindo...");
                    System.exit(0);
                default:
                    System.out.println("opção invalida");
            }
        }
    }

    private static void performOperation(Calculator calculator, int choice, double num1, double num2) {
        double result = 0;
        String operation = switch (choice) {
            case 1 -> {
                result = calculator.add(num1, num2);
                yield "Adição";
            }
            case 2 -> {
                result = calculator.subtract(num1, num2);
                yield "Subtração";
            }
            case 3 -> {
                result = calculator.multiply(num1, num2);
                yield "Multiplicação";
            }
            case 4 -> {
                result = calculator.divide(num1, num2);
                yield "Divisão";
            }
            default -> "";
        };

        System.out.println("Resultado de " + operation + " " + num1 + " e " + num2 + " = " + result);
    }
}
