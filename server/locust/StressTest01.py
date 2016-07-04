from locust import HttpLocust, TaskSet, task
from random import randint

colls = ('collA', 'collB')
colls_limit = len(colls) - 1


def next_collection():
    global colls_limit, colls;
    return colls[randint(0, colls_limit)]


current_key = 0


def next_key():
    global current_key
    key = current_key
    current_key = (current_key + 1) % 1e2
    return key


samples = []
kb = 1024
mb = kb * kb
smin = 5 * mb
smax = 10 * mb
num_samples = kb
slicee = (smax - smin) / num_samples

print('Creating bigdata [%d, %d]...' % (smin, smax))

big = bytearray()
for i in range(0, smax):
    big.append(randint(65, 90))

print('Creating %d samples...' % num_samples)

for i in range(0, num_samples - 1):
    l = smin + i * slicee
    samples.append(buffer(big, 0, l))

samples.append(buffer(big));

for sample in samples:
    print('Sample with %d bytes' % len(sample))

samples_limit = len(samples) - 1


def next_sample():
    global samples
    return samples[randint(0, samples_limit)]


class Putter:
    def __init__(self, client):
        self.client = client
        self.col = next_collection()
        self.key = next_key()
        self.sample = next_sample()

    def put(self):
        print('Putting...')
        with self.client.put('/%s/%d' % (self.col, self.key), data=self.sample, catch_response=True) as response:
            if response.status_code == 201:
                response.success()
            else:
                response.failure('%d/%s' % (response.status_code, response.reason))

        return (self.col, self.key, self.sample)


class Getter(TaskSet):
    @task
    def get(self):
        putter = Putter(self.client)
        (col, key, sample) = putter.put()

        print('Getting %s/%d' % (col, key))

        with self.client.get('/%s/%d' % (col, key), catch_response=True, stream=True) as response:
            if response.status_code == 200:
                if buffer(response.content) == sample:
                    response.success()
                else:
                    response.failure('Ops! expected %d bytes equal to %d bytes' % (len(sample), len(response.content)))
            else:
                response.failure('%d/%s' % (response.status_code, response.reason))


class Strees(HttpLocust):
    task_set = Getter
    max_wait = 2000
    min_wait = 500
