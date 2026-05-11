for i in $(seq 1 100); do
  java go.test.TestShm11 || break
done

for i in $(seq 1 100); do
  java go.test.TestShm13 || break
done
