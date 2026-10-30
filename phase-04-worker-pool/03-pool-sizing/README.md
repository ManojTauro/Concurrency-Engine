# 03 Pool sizing

**Do:** implement the three formulas in `Sizing`, then `Sweep.run`. `Workloads` is given.

**Run:** `mvn -q test -Dtest='C03*'`

**Then, for the note (`../results/<date>-pool-sizing.md`):**
- Hypothesis first: for `cpuBurn` how many workers will give the best throughput on your machine? For `sleep(10)`?
- Run the sweep with 1, 2, 4, 8, 16, 32 workers for each workload and put the numbers in a table. Where is the knee? What happens after it?
- Compare with the formulas: does `Sizing.cpuBound` predict the CPU knee? Does `Sizing.blocking` predict the waiting one?
- Use Little's law to work out how many workers a service needs at 500 requests/s when a request takes 40 ms.

**Think about:** what is the third resource that pool size has to respect besides the CPU (connections, memory, the downstream service)?
