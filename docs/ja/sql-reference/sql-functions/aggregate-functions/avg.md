---
displayed_sidebar: docs
---

# avg

選択されたフィールドの平均値を返します。

オプションのフィールド DISTINCT パラメータを使用すると、加重平均を返すことができます。

## Syntax

```Haskell
AVG([DISTINCT] expr)
```

## Examples

```plain text
MySQL > SELECT datetime, AVG(cost_time)
FROM log_statis
group by datetime;
+---------------------+--------------------+
| datetime            | avg(`cost_time`)   |
+---------------------+--------------------+
| 2019-07-03 21:01:20 | 25.827794561933533 |
+---------------------+--------------------+

MySQL > SELECT datetime, AVG(distinct cost_time)
FROM log_statis
group by datetime;
+---------------------+---------------------------+
| datetime            | avg(DISTINCT `cost_time`) |
+---------------------+---------------------------+
| 2019-07-04 02:23:24 |        20.666666666666668 |
+---------------------+---------------------------+

```

## keyword

AVG