#!/usr/bin/env bash
# ==========================================================
# Script khoi chay nhanh DHOPM Stream Visualizer
# Truong Dai Hoc Da Lat - Khoa CNTT
# Sinh vien: Vo Cong Vinh (2312800), Nguyen Duc Tin (2312774), Ngo Quoc Truong Vi (2312796)
# ==========================================================

cd "$(dirname "$0")"

# Tu dong tim Maven neu chua co trong PATH
if ! command -v mvn &> /dev/null; then
    if [ -f "/Users/nthtam/.maven/apache-maven-3.9.6/bin/mvn" ]; then
        export PATH="/Users/nthtam/.maven/apache-maven-3.9.6/bin:$PATH"
    fi
fi

case "$1" in
    test)
        echo "🧪 Dang chay 18 Unit Tests xac thuc Lab 1..."
        mvn test
        ;;
    verify|runner)
        echo "📊 Dang chay Lab 1 Verification Runner (in bang doi chieu)..."
        mvn compile exec:java -Dexec.mainClass="vn.edu.dlu.dhopm.core.Lab1VerificationRunner"
        ;;
    *)
        echo "⚡ Dang khoi chay giao dien DHOPM Stream Visualizer (JavaFX)..."
        mvn javafx:run
        ;;
esac
