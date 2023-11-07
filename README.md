# The Sleep Barber Problem

This Java program demonstrates the classic synchronization problem known as "The Sleeping Barber" problem. In this problem, there is a barber who is either cutting a customer's hair or sleeping if there are no customers. Customers arrive at the barber shop, and if there are available chairs, they get a haircut. Otherwise, they leave.

## Program Overview

The program consists of three main classes:

1. `TheSleepBarber`: This class contains the `main` method and is the entry point of the program. It creates a `BarberShop` and starts the barber and customer generator threads.

2. `BarberShop`: This class represents the barber shop and contains semaphores and a queue to control the interactions between the barber and customers.

3. `Barber` and `CustomerGenerator`: These are thread classes. `Barber` represents the barber's behavior, and `CustomerGenerator` simulates the arrival of customers.

## Synchronization Elements

- `Semaphore` is used to control the number of customers and barbers in the shop.

- `mutex` is a semaphore that provides mutual exclusion to protect the shared data structure (the queue of waiting customers).

## How It Works

- Customers are generated at random intervals and arrive at the barber shop.

- Customers check if there are available chairs. If chairs are available, they get a haircut. If not, they leave the shop.

- The barber checks if there are customers in the queue. If the queue is empty, the barber goes to sleep. When a customer arrives, the barber wakes up, serves the customer, and goes back to sleep if there are no more customers.

- The program demonstrates the core concept of synchronization using semaphores and mutual exclusion to coordinate the behavior of the barber and customers.

## Running the Program

To run the program, execute the `TheSleepBarber` class. The output will display the interactions between the barber and customers, indicating when the barber is cutting hair, sleeping, and when customers arrive and leave.

## Author

This code example is provided as a learning resource and was created by the author for educational purposes. If you have any questions or need further assistance, please feel free to reach out.

Happy coding!
