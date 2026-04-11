import sys
import numpy as np

data = [float(x.strip()) for x in sys.stdin if x.strip()]

print(np.std(data))