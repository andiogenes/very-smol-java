declare i32 @printf(i8*, ...)

@digit_format = constant [4 x i8] c"%d\0A\00"
@float_format = constant [4 x i8] c"%g\0A\00"
@void_format = constant [4 x i8] c"\0A\00\00\00"

@Main_field = global i32 0

define i32 @Main_method() {
block_0:
 %0 = add i32 1, 0
 ret i32 %0
}

define void @Main_counting() {
block_0:
 %0 = alloca i32
 %1 = add i32 0, 0
 store i32 %1, i32* %0
 %2 = alloca i32
 %3 = load i32, i32* %0
 %4 = add i32 %3, 1
 store i32 %4, i32* %0
 %5 = add i32 %3, 0
 store i32 %5, i32* %2
 %6 = load i32, i32* %0
 store i32 %6, i32* %2
 %7 = load i32, i32* %2
 %8 = alloca i32
 %9 = load i32, i32* %0
 %10 = sub i32 %9, 1
 store i32 %10, i32* %0
 %11 = add i32 %9, 0
 store i32 %11, i32* %8
 %12 = load i32, i32* %0
 store i32 %12, i32* %8
 %13 = load i32, i32* %8
 %14 = add i32 0, 0
 store i32 %14, i32* %0
 %15 = load i32, i32* %0
 %16 = alloca i32
 %17 = load i32, i32* %0
 %18 = add i32 %17, 1
 store i32 %18, i32* %0
 %19 = add i32 %18, 0
 store i32 %19, i32* %16
 %20 = load i32, i32* %0
 store i32 %20, i32* %16
 %21 = load i32, i32* %16
 %22 = alloca i32
 %23 = load i32, i32* %0
 %24 = sub i32 %23, 1
 store i32 %24, i32* %0
 %25 = add i32 %24, 0
 store i32 %25, i32* %22
 %26 = load i32, i32* %0
 store i32 %26, i32* %22
 %27 = load i32, i32* %22
 ret void 
}

define void @Main_unaryPlus() {
block_0:
 %0 = alloca i32
 %1 = add i32 10, 0
 store i32 %1, i32* %0
 %2 = alloca double
 %3 = fadd double 1.0, 0.0
 store double %3, double* %2
 ret void 
}

define void @Main_unaryMinus() {
block_0:
 %0 = alloca i32
 %1 = add i32 10, 0
 %2 = sub i32 0, %1
 store i32 %2, i32* %0
 %3 = alloca double
 %4 = fadd double 1.0, 0.0
 %5 = fsub double 0.0, %4
 store double %5, double* %3
 ret void 
}

define void @Main_neg() {
block_0:
 %0 = alloca i32
 %1 = add i32 10, 0
 %2 = icmp eq i32 %1, 0
 %3 = zext i1 %2 to i32
 store i32 %3, i32* %0
 %4 = alloca i32
 %5 = add i32 20, 0
 %6 = icmp eq i32 %5, 0
 %7 = zext i1 %6 to i32
 store i32 %7, i32* %4
 %8 = alloca i32
 %9 = add i32 5, 0
 %10 = sub i32 0, %9
 %11 = icmp eq i32 %10, 0
 %12 = zext i1 %11 to i32
 store i32 %12, i32* %8
 %13 = alloca i32
 %14 = add i32 0, 0
 %15 = icmp eq i32 %14, 0
 %16 = zext i1 %15 to i32
 store i32 %16, i32* %13
 ret void 
}

define void @Main_mul() {
block_0:
 %0 = alloca i32
 %1 = add i32 3, 0
 %2 = add i32 5, 0
 %3 = sub i32 0, %2
 %4 = mul i32 %1, %3
 store i32 %4, i32* %0
 %5 = alloca i32
 %6 = add i32 3, 0
 %7 = add i32 0, 0
 %8 = mul i32 %6, %7
 store i32 %8, i32* %5
 %9 = alloca i32
 %10 = add i32 1, 0
 %11 = add i32 2, 0
 %12 = mul i32 %10, %11
 %13 = add i32 3, 0
 %14 = mul i32 %12, %13
 %15 = add i32 4, 0
 %16 = mul i32 %14, %15
 store i32 %16, i32* %9
 %17 = alloca double
 %18 = add i32 10, 0
 %19 = fadd double 0.5, 0.0
 %20 = sitofp i32 %18 to double
 %21 = fmul double %20, %19
 store double %21, double* %17
 %22 = alloca double
 %23 = fadd double 0.5, 0.0
 %24 = add i32 10, 0
 %25 = sitofp i32 %24 to double
 %26 = fmul double %23, %25
 store double %26, double* %22
 ret void 
}

define void @Main_div() {
block_0:
 %0 = alloca i32
 %1 = add i32 5, 0
 %2 = add i32 2, 0
 %3 = udiv i32 %1, %2
 store i32 %3, i32* %0
 %4 = alloca double
 %5 = fadd double 5.0, 0.0
 %6 = add i32 2, 0
 %7 = sitofp i32 %6 to double
 %8 = fdiv double %5, %7
 store double %8, double* %4
 %9 = alloca double
 %10 = add i32 0, 0
 %11 = fadd double 5.0, 0.0
 %12 = sitofp i32 %10 to double
 %13 = fdiv double %12, %11
 store double %13, double* %9
 %14 = alloca i32
 %15 = add i32 5, 0
 %16 = add i32 2, 0
 %17 = sub i32 0, %16
 %18 = udiv i32 %15, %17
 store i32 %18, i32* %14
 %19 = alloca i32
 %20 = add i32 5, 0
 %21 = sub i32 0, %20
 %22 = add i32 2, 0
 %23 = udiv i32 %21, %22
 store i32 %23, i32* %19
 %24 = alloca i32
 %25 = add i32 5, 0
 %26 = sub i32 0, %25
 %27 = add i32 2, 0
 %28 = sub i32 0, %27
 %29 = udiv i32 %26, %28
 store i32 %29, i32* %24
 ret void 
}

define void @Main_add() {
block_0:
 %0 = alloca i32
 %1 = add i32 1, 0
 %2 = add i32 2, 0
 %3 = add i32 %1, %2
 store i32 %3, i32* %0
 %4 = alloca i32
 %5 = add i32 1, 0
 %6 = add i32 2, 0
 %7 = sub i32 0, %6
 %8 = add i32 %5, %7
 store i32 %8, i32* %4
 %9 = alloca double
 %10 = add i32 1, 0
 %11 = fadd double 1.0, 0.0
 %12 = sitofp i32 %10 to double
 %13 = fadd double %12, %11
 store double %13, double* %9
 ret void 
}

define void @Main_sub() {
block_0:
 %0 = alloca i32
 %1 = add i32 1, 0
 %2 = add i32 2, 0
 %3 = sub i32 %1, %2
 store i32 %3, i32* %0
 %4 = alloca i32
 %5 = add i32 1, 0
 %6 = sub i32 0, %5
 %7 = add i32 2, 0
 %8 = sub i32 0, %7
 %9 = sub i32 %6, %8
 store i32 %9, i32* %4
 %10 = alloca double
 %11 = add i32 1, 0
 %12 = fadd double 0.0, 0.0
 %13 = sitofp i32 %11 to double
 %14 = fsub double %13, %12
 store double %14, double* %10
 %15 = alloca double
 %16 = fadd double 1.0, 0.0
 %17 = add i32 0, 0
 %18 = sitofp i32 %17 to double
 %19 = fsub double %16, %18
 store double %19, double* %15
 ret void 
}

define void @Main_comparison() {
block_0:
 %0 = alloca i32
 %1 = fadd double 1.0, 0.0
 %2 = add i32 0, 0
 %3 = sitofp i32 %2 to double
 %4 = fcmp ogt double %1, %3
 %5 = zext i1 %4 to i32
 store i32 %5, i32* %0
 %6 = alloca i32
 %7 = add i32 1, 0
 %8 = fadd double 0.0, 0.0
 %9 = sitofp i32 %7 to double
 %10 = fcmp ogt double %9, %8
 %11 = zext i1 %10 to i32
 store i32 %11, i32* %6
 %12 = alloca i32
 %13 = add i32 1, 0
 %14 = add i32 0, 0
 %15 = icmp sgt i32 %13, %14
 %16 = zext i1 %15 to i32
 store i32 %16, i32* %12
 %17 = alloca i32
 %18 = add i32 1, 0
 %19 = add i32 1, 0
 %20 = icmp sgt i32 %18, %19
 %21 = zext i1 %20 to i32
 store i32 %21, i32* %17
 %22 = alloca i32
 %23 = add i32 1, 0
 %24 = add i32 1, 0
 %25 = icmp sge i32 %23, %24
 %26 = zext i1 %25 to i32
 store i32 %26, i32* %22
 %27 = alloca i32
 %28 = add i32 1, 0
 %29 = add i32 2, 0
 %30 = icmp sge i32 %28, %29
 %31 = zext i1 %30 to i32
 store i32 %31, i32* %27
 %32 = alloca i32
 %33 = add i32 1, 0
 %34 = add i32 2, 0
 %35 = icmp sgt i32 %33, %34
 %36 = zext i1 %35 to i32
 store i32 %36, i32* %32
 %37 = alloca i32
 %38 = fadd double 1.0, 0.0
 %39 = add i32 0, 0
 %40 = sitofp i32 %39 to double
 %41 = fcmp olt double %38, %40
 %42 = zext i1 %41 to i32
 store i32 %42, i32* %37
 %43 = alloca i32
 %44 = add i32 1, 0
 %45 = fadd double 0.0, 0.0
 %46 = sitofp i32 %44 to double
 %47 = fcmp olt double %46, %45
 %48 = zext i1 %47 to i32
 store i32 %48, i32* %43
 %49 = alloca i32
 %50 = add i32 1, 0
 %51 = add i32 0, 0
 %52 = icmp slt i32 %50, %51
 %53 = zext i1 %52 to i32
 store i32 %53, i32* %49
 %54 = alloca i32
 %55 = add i32 1, 0
 %56 = add i32 1, 0
 %57 = icmp slt i32 %55, %56
 %58 = zext i1 %57 to i32
 store i32 %58, i32* %54
 %59 = alloca i32
 %60 = add i32 1, 0
 %61 = add i32 1, 0
 %62 = icmp sle i32 %60, %61
 %63 = zext i1 %62 to i32
 store i32 %63, i32* %59
 %64 = alloca i32
 %65 = add i32 1, 0
 %66 = add i32 2, 0
 %67 = icmp sle i32 %65, %66
 %68 = zext i1 %67 to i32
 store i32 %68, i32* %64
 %69 = alloca i32
 %70 = add i32 1, 0
 %71 = add i32 2, 0
 %72 = icmp slt i32 %70, %71
 %73 = zext i1 %72 to i32
 store i32 %73, i32* %69
 %74 = alloca i32
 %75 = add i32 0, 0
 %76 = add i32 1, 0
 %77 = icmp eq i32 %75, %76
 %78 = zext i1 %77 to i32
 store i32 %78, i32* %74
 %79 = alloca i32
 %80 = add i32 0, 0
 %81 = add i32 1, 0
 %82 = icmp ne i32 %80, %81
 %83 = zext i1 %82 to i32
 store i32 %83, i32* %79
 %84 = alloca i32
 %85 = add i32 0, 0
 %86 = add i32 0, 0
 %87 = icmp eq i32 %85, %86
 %88 = zext i1 %87 to i32
 store i32 %88, i32* %84
 %89 = alloca i32
 %90 = add i32 0, 0
 %91 = fadd double 0.0, 0.0
 %92 = sitofp i32 %90 to double
 %93 = fcmp oeq double %92, %91
 %94 = zext i1 %93 to i32
 store i32 %94, i32* %89
 ret void 
}

define void @Main_and() {
block_0:
 %0 = alloca i32
 %1 = add i32 1, 0
 %2 = add i32 0, 0
 %3 = icmp ne i32 %1, 0
 %4 = zext i1 %3 to i32
 %5 = icmp ne i32 %2, 0
 %6 = zext i1 %5 to i32
 %7 = and i32 %4, %6
 store i32 %7, i32* %0
 %8 = alloca i32
 %9 = add i32 0, 0
 %10 = add i32 1, 0
 %11 = icmp ne i32 %9, 0
 %12 = zext i1 %11 to i32
 %13 = icmp ne i32 %10, 0
 %14 = zext i1 %13 to i32
 %15 = and i32 %12, %14
 store i32 %15, i32* %8
 %16 = alloca i32
 %17 = add i32 1, 0
 %18 = add i32 1, 0
 %19 = icmp ne i32 %17, 0
 %20 = zext i1 %19 to i32
 %21 = icmp ne i32 %18, 0
 %22 = zext i1 %21 to i32
 %23 = and i32 %20, %22
 store i32 %23, i32* %16
 %24 = alloca i32
 %25 = add i32 0, 0
 %26 = add i32 0, 0
 %27 = icmp ne i32 %25, 0
 %28 = zext i1 %27 to i32
 %29 = icmp ne i32 %26, 0
 %30 = zext i1 %29 to i32
 %31 = and i32 %28, %30
 store i32 %31, i32* %24
 %32 = alloca i32
 %33 = add i32 10, 0
 %34 = add i32 1, 0
 %35 = icmp ne i32 %33, 0
 %36 = zext i1 %35 to i32
 %37 = icmp ne i32 %34, 0
 %38 = zext i1 %37 to i32
 %39 = and i32 %36, %38
 store i32 %39, i32* %32
 ret void 
}

define void @Main_or() {
block_0:
 %0 = alloca i32
 %1 = add i32 1, 0
 %2 = add i32 0, 0
 %3 = icmp ne i32 %1, 0
 %4 = zext i1 %3 to i32
 %5 = icmp ne i32 %2, 0
 %6 = zext i1 %5 to i32
 %7 = or i32 %4, %6
 store i32 %7, i32* %0
 %8 = alloca i32
 %9 = add i32 0, 0
 %10 = add i32 1, 0
 %11 = icmp ne i32 %9, 0
 %12 = zext i1 %11 to i32
 %13 = icmp ne i32 %10, 0
 %14 = zext i1 %13 to i32
 %15 = or i32 %12, %14
 store i32 %15, i32* %8
 %16 = alloca i32
 %17 = add i32 1, 0
 %18 = add i32 1, 0
 %19 = icmp ne i32 %17, 0
 %20 = zext i1 %19 to i32
 %21 = icmp ne i32 %18, 0
 %22 = zext i1 %21 to i32
 %23 = or i32 %20, %22
 store i32 %23, i32* %16
 %24 = alloca i32
 %25 = add i32 0, 0
 %26 = add i32 0, 0
 %27 = icmp ne i32 %25, 0
 %28 = zext i1 %27 to i32
 %29 = icmp ne i32 %26, 0
 %30 = zext i1 %29 to i32
 %31 = or i32 %28, %30
 store i32 %31, i32* %24
 %32 = alloca i32
 %33 = add i32 10, 0
 %34 = add i32 1, 0
 %35 = icmp ne i32 %33, 0
 %36 = zext i1 %35 to i32
 %37 = icmp ne i32 %34, 0
 %38 = zext i1 %37 to i32
 %39 = or i32 %36, %38
 store i32 %39, i32* %32
 ret void 
}

define void @Main_complex() {
block_0:
 %0 = alloca i32
 %1 = add i32 1, 0
 %2 = add i32 0, 0
 %3 = icmp ne i32 %1, 0
 %4 = zext i1 %3 to i32
 %5 = icmp ne i32 %2, 0
 %6 = zext i1 %5 to i32
 %7 = or i32 %4, %6
 %8 = add i32 0, 0
 %9 = add i32 1, 0
 %10 = icmp ne i32 %8, 0
 %11 = zext i1 %10 to i32
 %12 = icmp ne i32 %9, 0
 %13 = zext i1 %12 to i32
 %14 = or i32 %11, %13
 %15 = icmp ne i32 %7, 0
 %16 = zext i1 %15 to i32
 %17 = icmp ne i32 %14, 0
 %18 = zext i1 %17 to i32
 %19 = and i32 %16, %18
 %20 = add i32 0, 0
 %21 = icmp ne i32 %19, 0
 %22 = zext i1 %21 to i32
 %23 = icmp ne i32 %20, 0
 %24 = zext i1 %23 to i32
 %25 = or i32 %22, %24
 store i32 %25, i32* %0
 %26 = alloca i32
 %27 = add i32 3, 0
 %28 = add i32 2, 0
 %29 = icmp sgt i32 %27, %28
 %30 = zext i1 %29 to i32
 %31 = add i32 3, 0
 %32 = add i32 3, 0
 %33 = icmp sle i32 %31, %32
 %34 = zext i1 %33 to i32
 %35 = add i32 1, 0
 %36 = icmp eq i32 %34, %35
 %37 = zext i1 %36 to i32
 %38 = icmp ne i32 %30, 0
 %39 = zext i1 %38 to i32
 %40 = icmp ne i32 %37, 0
 %41 = zext i1 %40 to i32
 %42 = and i32 %39, %41
 store i32 %42, i32* %26
 %43 = alloca i32
 %44 = add i32 5, 0
 %45 = add i32 8, 0
 %46 = mul i32 %44, %45
 %47 = add i32 32, 0
 %48 = add i32 2, 0
 %49 = udiv i32 %47, %48
 %50 = add i32 %46, %49
 %51 = sub i32 0, %50
 store i32 %51, i32* %43
 %52 = alloca double
 %53 = add i32 5, 0
 %54 = add i32 8, 0
 %55 = mul i32 %53, %54
 %56 = add i32 32, 0
 %57 = add i32 2, 0
 %58 = udiv i32 %56, %57
 %59 = add i32 %55, %58
 %60 = add i32 1, 0
 %61 = fadd double 3.0, 0.0
 %62 = sitofp i32 %60 to double
 %63 = fdiv double %62, %61
 %64 = sitofp i32 %59 to double
 %65 = fadd double %64, %63
 store double %65, double* %52
 %66 = alloca double
 %67 = load i32, i32* %43
 %68 = load double, double* %52
 %69 = sitofp i32 %67 to double
 %70 = fadd double %69, %68
 %71 = load i32, i32* %26
 %72 = sitofp i32 %71 to double
 %73 = fmul double %70, %72
 %74 = load i32, i32* @Main_field
 %75 = sitofp i32 %74 to double
 %76 = fadd double %73, %75
 store double %76, double* %66
 %77 = load double, double* %66
 %78 = call i32 () @Main_method()
 %79 = sitofp i32 %78 to double
 %80 = fmul double %77, %79
 store double %80, double* %66
 %81 = load double, double* %66
 ret void 
}

define void @Main_main() {
block_0:
 call void () @Main_counting()
 call void () @Main_unaryPlus()
 call void () @Main_unaryMinus()
 call void () @Main_neg()
 call void () @Main_mul()
 call void () @Main_div()
 call void () @Main_add()
 call void () @Main_sub()
 call void () @Main_comparison()
 call void () @Main_and()
 call void () @Main_or()
 call void () @Main_complex()
 ret void 
}

define void @field_init() {
init:
 %0 = add i32 10, 0
 %1 = add i32 5, 0
 %2 = add i32 4, 0
 %3 = mul i32 %1, %2
 %4 = add i32 %0, %3
 %5 = add i32 2, 0
 %6 = sub i32 %4, %5
 %7 = add i32 6, 0
 %8 = udiv i32 %6, %7
 store i32 %8, i32* @Main_field
 ret void 
}

define i32 @main() {
 call void @field_init()
 call void @Main_main()
 ret i32 0
}
