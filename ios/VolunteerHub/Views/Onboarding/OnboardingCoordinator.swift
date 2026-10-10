import SwiftUI

struct OnboardingCoordinator: View {
    @Environment(AppState.self) private var appState
    
    enum Step {
        case welcome
        case signIn
        case createAccount
        case profileSetup
        case majorSetup
        case loading
    }
    
    @State private var path = NavigationPath()
    
    // Collected Data
    @State private var email = ""
    @State private var password = ""
    @State private var fullName = ""
    @State private var grade = "11th Grade"
    @State private var major = "Computer Science"
    
    var body: some View {
        NavigationStack(path: $path) {
            WelcomeView(path: $path)
                .navigationDestination(for: Step.self) { step in
                    switch step {
                    case .welcome:
                        WelcomeView(path: $path)
                    case .signIn:
                        SignInView()
                    case .createAccount:
                        CreateAccountView(path: $path, email: $email, password: $password)
                    case .profileSetup:
                        ProfileSetupView(path: $path, fullName: $fullName, grade: $grade)
                    case .majorSetup:
                        MajorSetupView(path: $path, major: $major)
                    case .loading:
                        MatchingLoadingView {
                            // Finish flow
                            Task {
                                try? await appState.signIn(email: email, password: password)
                            }
                        }
                    }
                }
        }
    }
}

// MARK: - Welcome
struct WelcomeView: View {
    @Binding var path: NavigationPath
    
    var body: some View {
        VStack(spacing: 32) {
            Spacer()
            
            Image(systemName: "hands.and.sparkles.fill")
                .font(.system(size: 80))
                .foregroundStyle(.blue)
            
            VStack(spacing: 12) {
                Text("Volunteer Hub")
                    .font(.largeTitle.weight(.bold))
                Text("Find service hours that perfectly match your major and future career.")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            
            Spacer()
            
            VStack(spacing: 16) {
                Button {
                    path.append(OnboardingCoordinator.Step.createAccount)
                } label: {
                    Text("Get Started")
                        .font(.headline)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .foregroundStyle(.white)
                        .background(Color.blue)
                        .clipShape(Capsule())
                }
                
                Button {
                    path.append(OnboardingCoordinator.Step.signIn)
                } label: {
                    Text("I already have an account")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(.blue)
                }
            }
            .padding(32)
        }
        .navigationBarBackButtonHidden()
    }
}

// MARK: - Create Account
struct CreateAccountView: View {
    @Binding var path: NavigationPath
    @Binding var email: String
    @Binding var password: String
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Create Account")
                        .font(.title2.weight(.bold))
                    Text("Your password is securely encrypted and never stored in plain text.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 8)
                .listRowBackground(Color.clear)
            }
            
            Section {
                TextField("Email address", text: $email)
                    .textContentType(.emailAddress)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                SecureField("Password (min 6 chars)", text: $password)
                    .textContentType(.newPassword)
            }
            
            Section {
                Button {
                    path.append(OnboardingCoordinator.Step.profileSetup)
                } label: {
                    Text("Continue")
                        .frame(maxWidth: .infinity)
                }
                .disabled(email.isEmpty || password.count < 6)
            }
        }
    }
}

// MARK: - Profile Setup
struct ProfileSetupView: View {
    @Binding var path: NavigationPath
    @Binding var fullName: String
    @Binding var grade: String
    
    let grades = ["9th Grade", "10th Grade", "11th Grade", "12th Grade"]
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("About You")
                        .font(.title2.weight(.bold))
                    Text("This helps us verify your hours.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 8)
                .listRowBackground(Color.clear)
            }
            
            Section {
                TextField("Full Name", text: $fullName)
                    .textContentType(.name)
                
                Picker("Grade Level", selection: $grade) {
                    ForEach(grades, id: \.self) { g in
                        Text(g).tag(g)
                    }
                }
            }
            
            Section {
                Button {
                    path.append(OnboardingCoordinator.Step.majorSetup)
                } label: {
                    Text("Continue")
                        .frame(maxWidth: .infinity)
                }
                .disabled(fullName.isEmpty)
            }
        }
    }
}

// MARK: - Major Setup
struct MajorSetupView: View {
    @Binding var path: NavigationPath
    @Binding var major: String
    
    let majors = ["Pre-Med / Healthcare", "Computer Science", "Business / Finance", "Engineering", "Education", "Law / Political Science"]
    
    var body: some View {
        Form {
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Your Path")
                        .font(.title2.weight(.bold))
                    Text("What do you want to study in college? Our AI will match you with relevant volunteering.")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 8)
                .listRowBackground(Color.clear)
            }
            
            Section {
                Picker("Intended Major", selection: $major) {
                    ForEach(majors, id: \.self) { m in
                        Text(m).tag(m)
                    }
                }
                .pickerStyle(.inline)
            }
            
            Section {
                Button {
                    path.append(OnboardingCoordinator.Step.loading)
                } label: {
                    Text("Find Matches")
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }
}

// MARK: - Loading Screen
struct MatchingLoadingView: View {
    let onComplete: () -> Void
    @State private var isSpinning = false
    
    var body: some View {
        VStack(spacing: 32) {
            ZStack {
                Circle()
                    .stroke(Color.blue.opacity(0.2), lineWidth: 8)
                    .frame(width: 100, height: 100)
                
                Circle()
                    .trim(from: 0, to: 0.7)
                    .stroke(Color.blue, style: StrokeStyle(lineWidth: 8, lineCap: .round))
                    .frame(width: 100, height: 100)
                    .rotationEffect(Angle(degrees: isSpinning ? 360 : 0))
                    .animation(.linear(duration: 1).repeatForever(autoreverses: false), value: isSpinning)
                
                Image(systemName: "sparkles")
                    .font(.title)
                    .foregroundStyle(.blue)
            }
            
            Text("Finding the best volunteering opportunities for you...")
                .font(.headline)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 40)
        }
        .navigationBarBackButtonHidden()
        .onAppear {
            isSpinning = true
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                onComplete()
            }
        }
    }
}
