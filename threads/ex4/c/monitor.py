import multiprocessing
import time
import random

def producer(condition, buffer):
    while True:
        item = random.randint(1, 100)
        time.sleep(random.random()*2)
        with condition:
            while buffer.full():
                condition.wait()
            buffer.put(item)
            print(f'Produziu {item}')
            condition.notify()

def consumer(condition, buffer):

    while True:
        time.sleep(random.random()*2)
        with condition:
            condition.wait()
            item = buffer.get()
            print(f'Consumiu {item}')
            condition.notify()

if __name__ == '__main__':

    condition = multiprocessing.Condition()
    buffer = multiprocessing.Queue(10)

    producer_process = multiprocessing.Process(target=producer, args=(condition, buffer))
    consumer_process = multiprocessing.Process(target=consumer, args=(condition, buffer))

    producer_process.start()
    consumer_process.start()

    time.sleep(450)

    producer_process.join()
    consumer_process.join()
 