declare i32 @printf(i8*, ...)

@digit_format = constant [4 x i8] c"%d\0A\00"
@float_format = constant [4 x i8] c"%g\0A\00"
@void_format = constant [4 x i8] c"\0A\00\00\00"

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

define void @Main_main() {
block_0:
 %0 = add i32 7, 0
 store i32 %0, i32* @Main_param
 %1 = load i32, i32* @Main_param
 %2 = alloca i32
 %3 = call i32 () @Main_factorial()
 store i32 %3, i32* %2
 %4 = load i32, i32* %2
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %4)
 ret void 
}

define void @field_init() {
init:
 %0 = add i32 0, 0
 store i32 %0, i32* @Main_param
 ret void 
}

define i32 @main() {
 call void @field_init()
 call void @Main_main()
 ret i32 0
}
