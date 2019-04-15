package semproject;

/**
 *
 * @authors Aya Alloush & Nour Salman
 *
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
//import java.lang.Integer;

public class SemProject {

    static int jobHeapSize;

    static void printResult(int[] result, String algo) {
        System.out.println("-------------------- " + algo + " ----------------------");
        for (int i = 0; i < result.length; i++) {
            System.out.println("Job " + i + " on machine " + result[i]);
        }
        System.out.println();
    }

    static void swap(int[] arr, int a, int b) {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    static void swap(SpanPair[] arr, int a, int b) {
        SpanPair temp = new SpanPair(arr[a].load, arr[a].index);
        arr[a].load = arr[b].load;
        arr[a].index = arr[b].index;
        arr[b].load = temp.load;
        arr[b].index = temp.index;
    }

    static SpanPair extractMax(SpanPair arr[]) {
        SpanPair top = new SpanPair(arr[0].load, arr[0].index);
        swap(arr, 0, jobHeapSize - 1);
        heapifyMax(arr, --jobHeapSize, 0);
        return top;
    }

    static void heapifyMin(SpanPair arr[], int n, int i) {
        if (n == 1) {
            return;
        }
        int mini = i, l = 2 * i + 1, r = 2 * i + 2;
        if (r < n && arr[r].load <= arr[mini].load) {
            mini = r;
        }
        if (l < n && arr[l].load <= arr[mini].load) {
            mini = l;
        }
        if (mini != i) {// if subtree root has been changed
            swap(arr, i, mini);
            heapifyMin(arr, n, mini);
        }
    }

    static void heapifyMax(SpanPair arr[], int n, int i) {
        if (n == 1) {
            return;
        }
        int mx = i, l = 2 * i + 1, r = 2 * i + 2;
        if (r < n && arr[r].load >= arr[mx].load) {
            mx = r;
        }
        if (l < n && arr[l].load >= arr[mx].load) {
            mx = l;
        }
        if (mx != i) {
            swap(arr, i, mx);
            heapifyMax(arr, n, mx);
        }
    }

    static void buildHeap(SpanPair arr[], int n, boolean isMaxHeap) {
        if (isMaxHeap) {
            for (int i = n / 2 - 1; i >= 0; i--) {
                heapifyMax(arr, n, i);
            }
        } else {
            for (int i = n / 2 - 1; i >= 0; i--) {
                heapifyMin(arr, n, i);
            }
        }
    }

    public static void HeapSort(SpanPair[] arr) {
        int n = arr.length;
        buildHeap(arr, n, false);
        for (int i = n - 1; i >= 0; i--) {// put values in the array in descending order
            swap(arr, 0, i);
            heapifyMin(arr, i, 0);
        }
    }

    static int minLoadMach(SpanPair[] mach, int m) {
        double mini = Double.MAX_VALUE;
        int j = 0;
        for (int i = 0; i < m; i++) {

            if (mach[i].load <= mini) {
                j = i;
                mini = mach[i].load;
            }
        }
        return j;
    }

    public static void init(double[] origin, SpanPair[] job, SpanPair[] mach, int[] result) {
        for (int i = 0; i < job.length; i++) {
            job[i] = new SpanPair(origin[i], i);
        }
        for (int i = 0; i < mach.length; i++) {
            mach[i] = new SpanPair(0, i);
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int t, n, m;
        int[] result;
        double[] origin;
        SpanPair[] job, mach;

        StringBuilder sb = new StringBuilder();
        try (FileWriter writer = new FileWriter("Makespan.csv")) {
            sb.append("n,");
            sb.append("Simple,");
            sb.append("Up to Average,");
            sb.append("extractMax,");
            sb.append("Heap Sort,");
            sb.append("Partition,");
            sb.append("Partition DP\n");
            t = input.nextInt();
            while (t > 0) {
                System.out.println("\n----------------------------------------     "
                        + "n = " + t + "     ----------------------------------------\n");
                n = t--;
                sb.append(n).append(",");
                m = 2;
                job = new SpanPair[n];
                mach = new SpanPair[m];
                result = new int[n];
                origin = new double[n]; //array used to initialize job[] for each method
                Random random = new Random();
                System.out.println("Input :");
                for (int i = 0; i < n; i++) {
                    //origin[i] = input.nextInt();
                    origin[i] = random.nextInt(100000) + 1;
                    System.out.print(origin[i]);
                    if ((i + 1) % 5 == 0) {
                        System.out.println();
                    } else {
                        System.out.print(" ");
                    }
                }
                System.out.println();
//////////////////Method #1: Simple, no sorting
                init(origin, job, mach, result);
                long startClock, pauseClock;
                startClock = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    int mlm = minLoadMach(mach, m);
                    mach[mlm].load += job[i].load;
                    result[i] = mlm;
                }
                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append(",");

                printResult(result, "Simple");
                System.out.println("dif = " + Math.abs(mach[0].load - mach[1].load));
//////////////////Method #2: Fill each machine up to the total average
                init(origin, job, mach, result);
                startClock = System.nanoTime();
                double avg = 0;
                boolean[] taken = new boolean[n];
                for (int i = 0; i < n; i++) {
                    avg += job[i].load;
                }
                avg /= n;
                // we could've used a set rather than iterating over the whole job[] array
                // every time, but we decided to keep it simple..
                for (int i = 0; i < m; i++) {   // i'th machine
                    for (int j = 0; j < n; j++) {    // j'th job
                        if (!taken[j] && (mach[i].load + job[j].load) <= avg) {
                            taken[j] = true;
                            mach[i].load += job[j].load;
                            result[j] = i;
                        }
                    }
                }
                for (int j = 0; j < n; j++) {
                    if (!taken[j]) {
                        int mlm = minLoadMach(mach, m);
                        mach[mlm].load += job[j].load;
                        result[j] = mlm;
                    }
                }
                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append(",");
                printResult(result, "Up to Average");
                System.out.println("dif = " + Math.abs(mach[0].load - mach[1].load));
//////////////////Method #3: getting job with longest processing time using extractMax()
                init(origin, job, mach, result);
                startClock = System.nanoTime();
                jobHeapSize = n;
                buildHeap(job, n, true);
                for (int i = 0; i < n; i++) {
                    SpanPair maxJob = extractMax(job);
                    mach[0].load += maxJob.load;
                    result[maxJob.index] = mach[0].index;
                    heapifyMin(mach, m, 0);
                }
                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append(",");
                printResult(result, "extractMax");
                System.out.println("dif = " + Math.abs(mach[0].load - mach[1].load));
//////////////////Method #4: sorting jobs in descending order using min heap
                init(origin, job, mach, result);
                startClock = System.nanoTime();
                HeapSort(job);
                for (int i = 0; i < n; i++) {
                    mach[0].load += job[i].load;
                    result[job[i].index] = mach[0].index;
                    heapifyMin(mach, m, 0);
                }
                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append(",");
                printResult(result, "Heap Sort");
                System.out.println("dif = " + Math.abs(mach[0].load - mach[1].load));

///////////////////Method #5: works only when m = 2 , Partition - Subsets
                init(origin, job, mach, result);
                startClock = System.nanoTime();
                double dif = Double.MAX_VALUE, load1 = 0, load2 = 0;
                int leadZeros = 0;
                long mach1 = 1, lastSbst = (1L << n) / 2;   ////////
                String s;
                for (long i = 1; i < lastSbst; i++) {
                    s = Long.toBinaryString(i);
                    leadZeros = n - s.length();
                    for (int j = 0; j < leadZeros; j++) {
                        load2 += job[j].load;
                    }
                    for (int j = 0; j < s.length(); j++) {
                        if (s.charAt(j) == '1') {
                            load1 += job[leadZeros + j].load;
                        } else {
                            load2 += job[leadZeros + j].load;
                        }
                    }
                    if (Math.abs(load1 - load2) < dif) {
                        dif = Math.abs(load1 - load2);
                        mach1 = i;
                        //System.out.println("mach1 = "+mach1+" dif = "+dif);
                    }
                    load1 = load2 = 0;
                }
                s = Long.toBinaryString(mach1);
                leadZeros = n - s.length();
                for (int i = 0; i < leadZeros; i++) {
                    result[i] = 0;
                }
                for (int i = 0; i < s.length(); i++) {
                    result[i + leadZeros] = s.charAt(i) - '0';
                }
                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append(",");
                printResult(result, "Partition");
                System.out.println("dif = " + (n > 1 ? dif : job[0].load) + "\n");
//////////////////Method #6: works only with integers & when m = 2 , Partition - DP
                init(origin, job, mach, result);
                int[] task = new int[n];//An integer array specifically for this method
                for (int i = 0; i < n; i++) {
                    task[i] = (int) origin[i];
                }
                startClock = System.nanoTime();
                // Calculate sum of all elements 
                int sum = 0;
                for (int i = 0; i < n; i++) {
                    sum += task[i];
                }
                boolean dp[][] = new boolean[n + 1][sum / 2 + 1];
                //store first task that adds up to the i'th sum
                int[] firstTask = new int[sum / 2 + 1];
                Arrays.fill(firstTask, -1);
                // 0 sum is possible with the empty subset.
                firstTask[0] = 0;
                for (int i = 0; i <= n; i++) {
                    dp[i][0] = true;
                }
                //With 0 elements, only 0 is possible 
                for (int i = 1; i <= sum / 2; i++) {
                    dp[0][i] = false;
                }
                // Fill the partition table in bottom up manner 
                for (int i = 1; i <= n; i++) {
                    for (int j = 1; j <= sum / 2; j++) {
                        // If i'th element is excluded 
                        dp[i][j] = dp[i - 1][j];
                        // If i'th element is included 
                        if (task[i - 1] <= j) {
                            dp[i][j] |= dp[i - 1][j - task[i - 1]];
                        }
                        if (dp[i][j] && firstTask[j] == -1) {
                            firstTask[j] = i - 1;
                        }
                    }
                }
                //Find the maximum possible sum which allows for minimum difference/makespan
                int last = 0;
                for (int i = sum / 2; i > 0; i--) {
                    if (dp[n][i]) {
                        last = i;
                        break;
                    }
                }
                //Iterating backwards to get the elements of first subset
                int sbstSum = last;
                while (sbstSum > 0) {
                    result[firstTask[sbstSum]] = 1;
                    sbstSum -= task[firstTask[sbstSum]];
                }

                pauseClock = System.nanoTime();
                sb.append((double) (pauseClock - startClock) / 1000000).append("\n");
                printResult(result, "Partition DP");
                System.out.println("dif = " + (sum - 2 * last) + "\n");
                // Printing DP Matrix for testing
//                System.out.println("last = "+last);
//                for(int i=0;i<n+1;i++){
//                    for(int j=0;j<sum/2+1;j++)
//                        System.out.print(dp[i][j]+" ");
//                    System.out.println();
//                }
            }
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
