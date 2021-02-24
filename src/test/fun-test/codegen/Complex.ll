declare i32 @printf(i8*, ...)

@digit_format = constant [4 x i8] c"%d\0A\00"
@float_format = constant [4 x i8] c"%g\0A\00"
@void_format = constant [4 x i8] c"\0A\00\00\00"

@Main_Nested_foo = global i32 0

@Main_Nested_bar = global i32 0

@Main_Nested_baz = global i32 0

@Main_Nested_foobar = global double 0.0

define void @Main_Nested_method1() {
block_0:
 %0 = add i32 12345678, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %0)
 ret void 
}

define i32 @Main_Nested_method2() {
block_0:
 %0 = add i32 1, 0
 ret i32 %0
}

define void @Main_bar() {
block_0:
 %0 = add i32 987654321, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %0)
 ret void 
}

@Main_param = global i32 0

define i32 @Main_factorial() {
block_0:
 %0 = alloca i1
 store i1 false, i1* %0
 %1 = load i32, i32* @Main_param
 %2 = add i32 1, 0
 %3 = icmp sle i32 %1, %2
 %4 = zext i1 %3 to i32
 %5 = add i32 1, 0
 %6 = icmp eq i32 %5, %4
 %7 = load i1, i1* %0
 %8 = or i1 %7, %6
 br i1 %8, label %switch_0_0, label %switch_0_1
switch_0_0:
 store i1 true, i1* %0
 %9 = add i32 1, 0
 ret i32 %9
block_1:
 br label %switch_0_1
switch_0_1:
 br label %switch_0_2
switch_0_2:
 br label %switch_0_end
switch_0_end:
 %10 = alloca i32
 %11 = load i32, i32* @Main_param
 store i32 %11, i32* %10
 %12 = load i32, i32* @Main_param
 %13 = add i32 1, 0
 %14 = sub i32 %12, %13
 store i32 %14, i32* @Main_param
 %15 = load i32, i32* @Main_param
 %16 = load i32, i32* %10
 %17 = call i32 () @Main_factorial()
 %18 = mul i32 %16, %17
 ret i32 %18
}

define i32 @Main_intMethod() {
block_0:
 %0 = add i32 9, 0
 ret i32 %0
}

define double @Main_doubleMethod() {
block_0:
 %0 = call i32 () @Main_intMethod()
 %1 = sitofp i32 %0 to double
 ret double %1
}

define void @Main_main() {
block_0:
 %0 = alloca i32
 %1 = add i32 0, 0
 store i32 %1, i32* %0
 %2 = alloca i32
 %3 = add i32 0, 0
 store i32 %3, i32* %2
 %4 = alloca i32
 %5 = add i32 0, 0
 store i32 %5, i32* %4
 %6 = alloca double
 %7 = fadd double 0.0, 0.0
 store double %7, double* %6
 %8 = alloca i32
 %9 = add i32 5, 0
 store i32 %9, i32* %8
 %10 = alloca i32
 %11 = add i32 25252534, 0
 store i32 %11, i32* %10
 %12 = alloca i32
 %13 = add i32 15, 0
 store i32 %13, i32* %12
 %14 = alloca double
 %15 = fadd double 1.0, 0.0
 store double %15, double* %14
 %16 = alloca i1
 store i1 false, i1* %16
 %17 = load i32, i32* %8
 %18 = add i32 1, 0
 %19 = icmp eq i32 %18, %17
 %20 = load i1, i1* %16
 %21 = or i1 %20, %19
 br i1 %21, label %switch_0_0, label %switch_0_1
switch_0_0:
 store i1 true, i1* %16
 br label %switch_0_1
switch_0_1:
 %22 = add i32 2, 0
 %23 = icmp eq i32 %22, %17
 %24 = load i1, i1* %16
 %25 = or i1 %24, %23
 br i1 %25, label %switch_0_2, label %switch_0_3
switch_0_2:
 store i1 true, i1* %16
 %26 = add i32 5, 0
 store i32 %26, i32* %8
 %27 = load i32, i32* %8
 br label %switch_0_3
switch_0_3:
 %28 = add i32 3, 0
 %29 = icmp eq i32 %28, %17
 %30 = load i1, i1* %16
 %31 = or i1 %30, %29
 br i1 %31, label %switch_0_4, label %switch_0_5
switch_0_4:
 store i1 true, i1* %16
 %32 = add i32 10, 0
 store i32 %32, i32* %8
 %33 = load i32, i32* %8
 br label %switch_0_5
switch_0_5:
 %34 = add i32 4, 0
 %35 = icmp eq i32 %34, %17
 %36 = load i1, i1* %16
 %37 = or i1 %36, %35
 br i1 %37, label %switch_0_6, label %switch_0_7
switch_0_6:
 store i1 true, i1* %16
 br label %switch_0_end
block_1:
 br label %switch_0_7
switch_0_7:
 %38 = add i32 5, 0
 %39 = icmp eq i32 %38, %17
 %40 = load i1, i1* %16
 %41 = or i1 %40, %39
 br i1 %41, label %switch_0_8, label %switch_0_9
switch_0_8:
 store i1 true, i1* %16
 %42 = add i32 42424242, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %42)
 br label %switch_0_end
block_2:
 br label %switch_0_9
switch_0_9:
 br label %switch_0_10
switch_0_10:
 br label %switch_0_end
switch_0_end:
 %44 = alloca i32
 %45 = add i32 10, 0
 store i32 %45, i32* %44
 %46 = add i32 15, 0
 %47 = sub i32 0, %46
 store i32 %47, i32* %44
 %48 = load i32, i32* %44
 store i32 %48, i32* %8
 %49 = load i32, i32* %8
 %50 = alloca i32
 %51 = load i32, i32* %8
 %52 = load i32, i32* %44
 %53 = icmp eq i32 %51, %52
 %54 = zext i1 %53 to i32
 store i32 %54, i32* %50
 %55 = alloca i32
 %56 = load i32, i32* %8
 %57 = load i32, i32* %44
 %58 = icmp ne i32 %56, %57
 %59 = zext i1 %58 to i32
 store i32 %59, i32* %55
 %60 = alloca i32
 %61 = load i32, i32* %50
 %62 = load i32, i32* %55
 %63 = icmp ne i32 %61, 0
 %64 = zext i1 %63 to i32
 %65 = icmp ne i32 %62, 0
 %66 = zext i1 %65 to i32
 %67 = or i32 %64, %66
 %68 = icmp eq i32 %67, 0
 %69 = zext i1 %68 to i32
 %70 = load i32, i32* %50
 %71 = load i32, i32* %55
 %72 = icmp eq i32 %71, 0
 %73 = zext i1 %72 to i32
 %74 = icmp ne i32 %70, 0
 %75 = zext i1 %74 to i32
 %76 = icmp ne i32 %73, 0
 %77 = zext i1 %76 to i32
 %78 = and i32 %75, %77
 %79 = icmp ne i32 %69, 0
 %80 = zext i1 %79 to i32
 %81 = icmp ne i32 %78, 0
 %82 = zext i1 %81 to i32
 %83 = or i32 %80, %82
 store i32 %83, i32* %60
 %84 = alloca i32
 %85 = add i32 5, 0
 %86 = add i32 5, 0
 %87 = icmp sgt i32 %85, %86
 %88 = zext i1 %87 to i32
 store i32 %88, i32* %84
 %89 = alloca i32
 %90 = add i32 7, 0
 %91 = add i32 8, 0
 %92 = icmp slt i32 %90, %91
 %93 = zext i1 %92 to i32
 store i32 %93, i32* %89
 %94 = alloca i32
 %95 = add i32 8, 0
 %96 = add i32 6, 0
 %97 = icmp sge i32 %95, %96
 %98 = zext i1 %97 to i32
 store i32 %98, i32* %94
 %99 = alloca i32
 %100 = add i32 4, 0
 %101 = add i32 4, 0
 %102 = icmp sle i32 %100, %101
 %103 = zext i1 %102 to i32
 store i32 %103, i32* %99
 %104 = add i32 5, 0
 %105 = add i32 5, 0
 %106 = add i32 %104, %105
 %107 = add i32 9, 0
 %108 = sub i32 %106, %107
 %109 = add i32 45, 0
 %110 = mul i32 %108, %109
 store i32 %110, i32* %8
 %111 = load i32, i32* %8
 %112 = load i32, i32* %8
 %113 = add i32 %112, 1
 store i32 %113, i32* %8
 %114 = add i32 %112, 0
 %115 = load i32, i32* %8
 %116 = add i32 %115, 1
 store i32 %116, i32* %8
 %117 = add i32 %116, 0
 %118 = load i32, i32* %8
 %119 = sub i32 %118, 1
 store i32 %119, i32* %8
 %120 = add i32 %119, 0
 %121 = load i32, i32* %8
 %122 = sub i32 %121, 1
 store i32 %122, i32* %8
 %123 = add i32 %121, 0
 %124 = load double, double* %14
 %125 = add i32 10, 0
 %126 = sitofp i32 %125 to double
 %127 = fdiv double %124, %126
 store double %127, double* %14
 %128 = load double, double* %14
 %129 = load double, double* %14
 %130 = load i32, i32* @Main_Nested_foo
 store i32 %130, i32* %8
 %131 = load i32, i32* %8
 call void () @Main_Nested_method1()
 call void () @Main_bar()
 %132 = add i32 7, 0
 store i32 %132, i32* @Main_param
 %133 = load i32, i32* @Main_param
 %134 = alloca i32
 %135 = call i32 () @Main_factorial()
 store i32 %135, i32* %134
 %136 = alloca i32
 %137 = call i32 () @Main_intMethod()
 %138 = add i32 2, 0
 %139 = udiv i32 %137, %138
 store i32 %139, i32* %136
 %140 = alloca double
 %141 = call double () @Main_doubleMethod()
 %142 = add i32 2, 0
 %143 = sitofp i32 %142 to double
 %144 = fdiv double %141, %143
 store double %144, double* %140
 ret void 
}

define void @field_init() {
init:
 %0 = add i32 0, 0
 store i32 %0, i32* @Main_Nested_foo
 %1 = add i32 0, 0
 store i32 %1, i32* @Main_Nested_bar
 %2 = add i32 0, 0
 store i32 %2, i32* @Main_Nested_baz
 %3 = fadd double 0.0, 0.0
 store double %3, double* @Main_Nested_foobar
 %4 = add i32 0, 0
 store i32 %4, i32* @Main_param
 ret void 
}

define i32 @main() {
 call void @field_init()
 call void @Main_main()
 ret i32 0
}
