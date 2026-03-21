package ru.otus.java.basic.lessons1; // пакеты (НЕ ЗАБЫВАТЬ!!!)

import java.util.Scanner;

public class App {

    public static void main(String[] args) {  // Главный класс выполнения приложения
        System.out.println("psvm - public static void main(String[] args)");

        // Калькулятор
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введи 1-е число:");
        int integer1 = scanner.nextInt();

        System.out.println("Введи 2-е число:");
        int integer2 = scanner.nextInt();

        System.out.println("Выбери операцию: 1 - сложение, 2 - вычитание, 3 - умножение, 4 - деление:");
        int operation = scanner.nextInt();

        // Вызов метода
         getCalculator(operation, integer1, integer2);
    }

    // Метод для выполнения операции
    public static void getCalculator(int operation, int integer1, int integer2) {
        if (operation == 1) {
            int total = integer1 + integer2;
            System.out.println("Сумма чисел = " + total);
        } else if (operation == 2) {
            int total = integer1 - integer2;
            System.out.println("Разница чисел = " + total);
        } else if (operation == 3) {
            int total = integer1 * integer2;
            System.out.println("Произведение чисел = " + total);
        } else if (operation == 4) {
            if (integer2 == 0) {
                System.out.println("На ноль делить нельзя!");
            } else {
                int total = integer1 / integer2;
                System.out.println("Результат деления = " + total);
            }
        } else {
            System.out.println("Неверная операция. Выберите число от 1 до 4.");
        }
    }
}
