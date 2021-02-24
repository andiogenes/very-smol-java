declare i32 @printf(i8*, ...)

@digit_format = constant [4 x i8] c"%d\0A\00"
@float_format = constant [4 x i8] c"%g\0A\00"
@void_format = constant [4 x i8] c"\0A\00\00\00"

@Main_x = global i32 0

define i32 @Main_firstReturn() {
block_0:
 %0 = add i32 1, 0
 ret i32 %0
block_1:
 %1 = add i32 2, 0
 ret i32 %1
block_2:
 %2 = add i32 3, 0
 ret i32 %2
block_3:
 %3 = add i32 4, 0
 ret i32 %3
}

define double @Main_switchReturn() {
block_0:
 %0 = alloca i1
 store i1 false, i1* %0
 %1 = load i32, i32* @Main_x
 %2 = add i32 0, 0
 %3 = icmp eq i32 %2, %1
 %4 = load i1, i1* %0
 %5 = or i1 %4, %3
 br i1 %5, label %switch_0_0, label %switch_0_1
switch_0_0:
 store i1 true, i1* %0
 %6 = add i32 1, 0
 store i32 %6, i32* @Main_x
 %7 = load i32, i32* @Main_x
 %8 = fadd double 12.1, 0.0
 ret double %8
block_1:
 br label %switch_0_1
switch_0_1:
 %9 = add i32 1, 0
 %10 = icmp eq i32 %9, %1
 %11 = load i1, i1* %0
 %12 = or i1 %11, %10
 br i1 %12, label %switch_0_2, label %switch_0_3
switch_0_2:
 store i1 true, i1* %0
 %13 = add i32 2, 0
 store i32 %13, i32* @Main_x
 %14 = load i32, i32* @Main_x
 %15 = fadd double 45.2, 0.0
 ret double %15
block_2:
 br label %switch_0_3
switch_0_3:
 br label %switch_0_4
switch_0_4:
 br label %switch_0_end
switch_0_end:
 %16 = fadd double 1.5, 0.0
 ret double %16
}

define void @Main_switchTest() {
block_0:
 %0 = alloca i1
 store i1 false, i1* %0
 %1 = load i32, i32* @Main_x
 %2 = add i32 0, 0
 %3 = icmp eq i32 %2, %1
 %4 = load i1, i1* %0
 %5 = or i1 %4, %3
 br i1 %5, label %switch_0_0, label %switch_0_1
switch_0_0:
 store i1 true, i1* %0
 %6 = add i32 10, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %6)
 br label %switch_0_1
switch_0_1:
 %8 = add i32 1, 0
 %9 = icmp eq i32 %8, %1
 %10 = load i1, i1* %0
 %11 = or i1 %10, %9
 br i1 %11, label %switch_0_2, label %switch_0_3
switch_0_2:
 store i1 true, i1* %0
 %12 = add i32 20, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %12)
 br label %switch_0_end
block_1:
 br label %switch_0_3
switch_0_3:
 %14 = add i32 2, 0
 %15 = icmp eq i32 %14, %1
 %16 = load i1, i1* %0
 %17 = or i1 %16, %15
 br i1 %17, label %switch_0_4, label %switch_0_5
switch_0_4:
 store i1 true, i1* %0
 %18 = add i32 30, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %18)
 br label %switch_0_5
switch_0_5:
 br label %switch_0_6
switch_0_6:
 %20 = add i32 40, 0
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %20)
 br label %switch_0_end
block_2:
 br label %switch_0_end
switch_0_end:
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @void_format, i32 0, i32 0))
 ret void 
}

define void @Main_main() {
block_0:
 %0 = call i32 () @Main_firstReturn()
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @digit_format, i32 0, i32 0), i32 %0)
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @void_format, i32 0, i32 0))
 %3 = call double () @Main_switchReturn()
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @float_format, i32 0, i32 0), double %3)
 %5 = call double () @Main_switchReturn()
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @float_format, i32 0, i32 0), double %5)
 %7 = call double () @Main_switchReturn()
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @float_format, i32 0, i32 0), double %7)
 call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @void_format, i32 0, i32 0))
 %10 = add i32 0, 0
 store i32 %10, i32* @Main_x
 %11 = load i32, i32* @Main_x
 call void () @Main_switchTest()
 %12 = add i32 1, 0
 store i32 %12, i32* @Main_x
 %13 = load i32, i32* @Main_x
 call void () @Main_switchTest()
 %14 = add i32 2, 0
 store i32 %14, i32* @Main_x
 %15 = load i32, i32* @Main_x
 call void () @Main_switchTest()
 %16 = add i32 3, 0
 store i32 %16, i32* @Main_x
 %17 = load i32, i32* @Main_x
 call void () @Main_switchTest()
 ret void 
}

define void @field_init() {
init:
 %0 = add i32 0, 0
 store i32 %0, i32* @Main_x
 ret void 
}

define i32 @main() {
 call void @field_init()
 call void @Main_main()
 ret i32 0
}
