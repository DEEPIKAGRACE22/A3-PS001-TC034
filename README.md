# A3-PS001-TC034: Android Ransomware-Like Activity Early Warning

Hackathon project for **AIORI Hackathon 3**, developed for PS-001 by Deepika Grace T T and Eliza Miriam Paul, with mentor Dr. Manu Elappila.

The project proposes an early-warning detector for ransomware-like file-system activity in a controlled Android lab. It focuses on observable bursts of file writes, renames, and extension changes in an explicitly accessible test directory, with the goal of raising an alert before a harmless simulation changes many files.

## Project presentation

The presentation covers the problem, Android constraints, related work, proposed detector, architecture, data flow, and planned implementation phases.

- [AIORI-3 project presentation](A3-PS001-TC034_AIORI3_Presentation.pptx)
- [Hackathon project structure proposal](Proposed-structure-hackathon.pdf)
- [Presentation video on YouTube](https://www.youtube.com/watch?v=wFdJ85psiuk)

## Problem and scope

Signature-based antivirus cannot identify a previously uncatalogued binary by signature. Ransomware-like activity can instead be studied through its file-system behaviour, such as rapid writes across many files, overwrites, renames, and changes to file extensions. On Android, app sandboxing and scoped storage limit which files an app can observe, so this work is deliberately constrained to a documented lab device and an app-owned or explicitly granted test directory.

The intended prototype scope is:

- One Android emulator or rooted lab device.
- One explicitly accessible test directory containing synthetic files.
- Two benign workloads, such as bulk copy or editor autosave, and three harmless simulations.
- Detection delay and files changed before alert, alongside precision, recall, false-positive rate, and detector resource use.
- Containment limited to stopping the simulator process started by the test harness.

This project does **not** claim to detect real malware, encrypt or recover user data, inspect other apps' memory on stock Android, or provide device-wide ransomware prevention.

## Proposed design

1. **Observe:** use Android `FileObserver` (backed by Linux inotify) to timestamp file events in the test directory.
2. **Extract features:** compute rolling-window signals, including writes per second, renames per second, unique files touched, extension changes, and event gaps.
3. **Detect:** combine fast behavioural rules with an anomaly score calibrated against benign workloads. Robust z-scores, median absolute deviation, and EWMA are proposed; a Markov sequence model is a stretch goal.
4. **Score and alert:** produce a 0-100 risk score with per-feature reasons and use sustained-window/hysteresis checks to reduce false alarms.
5. **Respond and log:** notify the user and, in the controlled test harness only, stop the simulation process it launched. Record events and decisions as JSONL for offline analysis.
6. **Evaluate:** use Python analysis and a planned Streamlit dashboard to compare rule-only, anomaly-only, and hybrid approaches across repeated runs.

The design targets early alerts while avoiding false positives from legitimate file bursts and low-rate simulations. These are hypotheses to test, not measured outcomes.

## Safety and limitations

- Testing is limited to synthetic files in a throw-away lab directory.
- Simulations are intended to be harmless and reversible, using a throw-away key; restore the dataset between runs.
- Monitoring is limited to files the lab app can explicitly access. `FileObserver` watches one directory at a time and is not recursive; subdirectories require separate watches.
- Event-queue overflow is logged as a telemetry gap rather than treated as complete data.
- Containment must target only the simulator process spawned by the harness.
- No performance results are claimed yet.

## Status

The proposal, detector design, and architecture are documented. **Implementation is pending**, so this repository currently includes the project presentation and proposed structure document, not a working detector or evaluation results. The planned phases are lab telemetry, workloads and simulations, rule detection, anomaly scoring, repeated-run evaluation, and a final demo/documentation package.

## References

- Android Developers: [Security best practices](https://developer.android.com/privacy-and-security/security-best-practices), [FileObserver API](https://developer.android.com/reference/android/os/FileObserver), [Data and file storage overview](https://developer.android.com/training/data-storage), and [Android Emulator](https://developer.android.com/studio/run/emulator).
- Linux man-pages: [inotify(7)](https://man7.org/linux/man-pages/man7/inotify.7.html).
- N. Scaife et al., “CryptoLock (and Drop It): Stopping Ransomware Attacks on User Data,” IEEE ICDCS, 2016.
- B. Caliskan et al., “RansomTrack: A Hybrid Behavioral Analysis Framework for Ransomware Detection,” arXiv:2604.08739, 2026.
- P. A. Gagniuc, “Antivirus Systems: Detection Methods and Architectures,” *Algorithms* 19(5), 345, 2026, [doi:10.3390/a19050345](https://doi.org/10.3390/a19050345).
- M. Hirano et al., “RanSAP: An open dataset of ransomware storage access patterns,” *Forensic Science International: Digital Investigation* 40, 301314, 2022.
- M. Hirano and R. Kobayashi, “RanSMAP: Open dataset of ransomware storage and memory access patterns,” *Computers & Security* 150, 104202, 2025.
- N. Andronio et al., “HelDroid: Dissecting and Detecting Mobile Ransomware,” RAID, 2015.
- K. Drabent et al., “How to circumvent and beat the ransomware in Android: a case study of Locker.cb!tr,” *Electronics* 13, 2212, 2024.
