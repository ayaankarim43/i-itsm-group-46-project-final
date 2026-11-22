package com.example.demo;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
class AccountPagesController {

    private static final Logger log =
            LoggerFactory.getLogger(AccountPagesController.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    private final PendingRegistrationRepository pendingRegistrations;
    private final PasswordResetChallengeRepository resetChallenges;

    private final EmailOtpService otpService;

    AccountPagesController(
            UserRepository users,
            PasswordEncoder encoder,
            PendingRegistrationRepository pendingRegistrations,
            PasswordResetChallengeRepository resetChallenges,
            EmailOtpService otpService) {

        this.users = users;
        this.encoder = encoder;
        this.pendingRegistrations = pendingRegistrations;
        this.resetChallenges = resetChallenges;
        this.otpService = otpService;
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String department,
            RedirectAttributes ra) {

        String cleanName = name.trim();
        String cleanEmail =
                email.trim().toLowerCase(Locale.ROOT);
        String cleanDepartment = department.trim();

        if (cleanName.isBlank()
                || cleanDepartment.isBlank()) {

            ra.addFlashAttribute(
                    "error",
                    "Name and department are required.");

            return "redirect:/register";
        }

        if (!isEmailSyntaxValid(cleanEmail)) {

            ra.addFlashAttribute(
                    "error",
                    "Please enter a valid email address.");

            return "redirect:/register";
        }

        if (password.length() < 8) {

            ra.addFlashAttribute(
                    "error",
                    "Password must contain at least 8 characters.");

            return "redirect:/register";
        }

        if (users.findByEmail(cleanEmail).isPresent()) {

            ra.addFlashAttribute(
                    "error",
                    "Email already registered.");

            return "redirect:/register";
        }

        /*
         * Remove any unfinished registration for this email.
         */
        pendingRegistrations.deleteByEmail(cleanEmail);

        String otp = otpService.generateOtp();

        PendingRegistration pending =
                new PendingRegistration(
                        cleanName,
                        cleanEmail,
                        cleanDepartment,
                        encoder.encode(password),
                        otpService.hash(otp),
                        otpService.expiresAt()
                );

        pendingRegistrations.save(pending);

        try {

            otpService.sendRegistrationOtp(
                    cleanEmail,
                    otp);

        } catch (MailException ex) {

            log.error(
                    "Failed to send registration OTP to {}",
                    cleanEmail,
                    ex);

            pendingRegistrations.deleteByEmail(
                    cleanEmail);

            ra.addFlashAttribute(
                    "error",
                    "We could not send the verification OTP. "
                            + "Please try again.");

            return "redirect:/register";

        } catch (RuntimeException ex) {

            log.error(
                    "Unexpected error while sending registration OTP to {}",
                    cleanEmail,
                    ex);

            pendingRegistrations.deleteByEmail(
                    cleanEmail);

            ra.addFlashAttribute(
                    "error",
                    "We could not send the verification OTP. "
                            + "Please try again.");

            return "redirect:/register";
        }

        ra.addFlashAttribute(
                "email",
                cleanEmail);

        ra.addFlashAttribute(
                "ok",
                "A verification code has been sent to your email.");

        return "redirect:/register/verify";
    }

    @GetMapping("/register/verify")
    String verifyRegistrationForm() {
        return "verify-registration";
    }

    @PostMapping("/register/verify")
    String verifyRegistration(
            @RequestParam String email,
            @RequestParam String otp,
            RedirectAttributes ra) {

        String cleanEmail =
                email.trim().toLowerCase(Locale.ROOT);

        PendingRegistration pending =
                pendingRegistrations
                        .findByEmail(cleanEmail)
                        .orElse(null);

        if (pending == null) {

            ra.addFlashAttribute(
                    "error",
                    "No pending registration was found.");

            return "redirect:/register";
        }

        if (otpService.expired(
                pending.getExpiresAt())) {

            pendingRegistrations.delete(pending);

            ra.addFlashAttribute(
                    "error",
                    "The verification code has expired.");

            return "redirect:/register";
        }

        if (otpService.tooManyAttempts(
                pending.getAttempts())) {

            pendingRegistrations.delete(pending);

            ra.addFlashAttribute(
                    "error",
                    "Too many incorrect attempts.");

            return "redirect:/register";
        }

        if (!otpService.verify(
                otp.trim(),
                pending.getOtpHash(),
                pending.getAttempts())) {

            pending.incrementAttempts();
            pendingRegistrations.save(pending);

            ra.addFlashAttribute(
                    "error",
                    "Incorrect verification code.");

            ra.addFlashAttribute(
                    "email",
                    cleanEmail);

            return "redirect:/register/verify";
        }

        if (users.findByEmail(cleanEmail).isPresent()) {

            pendingRegistrations.delete(pending);

            ra.addFlashAttribute(
                    "error",
                    "Email already registered.");

            return "redirect:/register";
        }

        Employee employee =
                new Employee(
                        pending.getName(),
                        pending.getEmail(),
                        pending.getDepartment());

        employee.changePassword(
                pending.getPasswordHash());

        users.save(employee);

        pendingRegistrations.delete(pending);

        ra.addFlashAttribute(
                "ok",
                "Account verified and created. You can now log in.");

        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    String forgotPassword(
            @RequestParam String email,
            RedirectAttributes ra) {

        String cleanEmail =
                email.trim().toLowerCase(Locale.ROOT);

        User user =
                users.findByEmail(cleanEmail)
                        .orElse(null);

        if (user == null) {

            ra.addFlashAttribute(
                    "error",
                    "That email address is not registered.");

            return "redirect:/forgot-password";
        }

        /*
         * Remove any previous password-reset request
         * for this email.
         */
        resetChallenges.deleteByEmail(cleanEmail);

        String otp = otpService.generateOtp();

        PasswordResetChallenge challenge =
                new PasswordResetChallenge(
                        cleanEmail,
                        otpService.hash(otp),
                        otpService.expiresAt());

        resetChallenges.save(challenge);

        try {

            otpService.sendPasswordResetOtp(
                    cleanEmail,
                    otp);

        } catch (MailException ex) {

            log.error(
                    "Failed to send password-reset OTP to {}",
                    cleanEmail,
                    ex);

            resetChallenges.deleteByEmail(
                    cleanEmail);

            ra.addFlashAttribute(
                    "error",
                    "We could not send the password-reset OTP. "
                            + "Please try again.");

            return "redirect:/forgot-password";

        } catch (RuntimeException ex) {

            log.error(
                    "Unexpected error while sending password-reset OTP to {}",
                    cleanEmail,
                    ex);

            resetChallenges.deleteByEmail(
                    cleanEmail);

            ra.addFlashAttribute(
                    "error",
                    "We could not send the password-reset OTP. "
                            + "Please try again.");

            return "redirect:/forgot-password";
        }

        ra.addFlashAttribute(
                "email",
                cleanEmail);

        ra.addFlashAttribute(
                "ok",
                "A password-reset OTP has been sent to your email.");

        return "redirect:/forgot-password/verify";
    }

    @GetMapping("/forgot-password/verify")
    String verifyResetForm() {
        return "verify-reset-otp";
    }

    @PostMapping("/forgot-password/verify")
    String verifyReset(
            @RequestParam String email,
            @RequestParam String otp,
            HttpSession session,
            RedirectAttributes ra) {

        String cleanEmail =
                email.trim().toLowerCase(Locale.ROOT);

        PasswordResetChallenge challenge =
                resetChallenges
                        .findTopByEmailOrderByIdDesc(cleanEmail)
                        .orElse(null);

        if (challenge == null
                || challenge.isUsed()) {

            ra.addFlashAttribute(
                    "error",
                    "No active password-reset request was found.");

            return "redirect:/forgot-password";
        }

        if (otpService.expired(
                challenge.getExpiresAt())) {

            resetChallenges.delete(challenge);

            ra.addFlashAttribute(
                    "error",
                    "The password-reset OTP has expired.");

            return "redirect:/forgot-password";
        }

        if (otpService.tooManyAttempts(
                challenge.getAttempts())) {

            resetChallenges.delete(challenge);

            ra.addFlashAttribute(
                    "error",
                    "Too many incorrect attempts.");

            return "redirect:/forgot-password";
        }

        if (!otpService.verify(
                otp.trim(),
                challenge.getOtpHash(),
                challenge.getAttempts())) {

            challenge.incrementAttempts();
            resetChallenges.save(challenge);

            ra.addFlashAttribute(
                    "error",
                    "Incorrect password-reset OTP.");

            ra.addFlashAttribute(
                    "email",
                    cleanEmail);

            return "redirect:/forgot-password/verify";
        }

        challenge.markUsed();
        resetChallenges.save(challenge);

        /*
         * The session now proves that this browser
         * successfully completed OTP verification.
         */
        session.setAttribute(
                "passwordResetVerifiedEmail",
                cleanEmail);

        ra.addFlashAttribute(
                "email",
                cleanEmail);

        return "redirect:/forgot-password/reset";
    }

    @GetMapping("/forgot-password/reset")
    String resetPasswordForm(
            HttpSession session,
            RedirectAttributes ra) {

        if (session.getAttribute(
                "passwordResetVerifiedEmail") == null) {

            ra.addFlashAttribute(
                    "error",
                    "Please verify the password-reset OTP first.");

            return "redirect:/forgot-password";
        }

        return "reset-password";
    }

    @PostMapping("/forgot-password/reset")
    String resetPassword(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes ra) {

        String cleanEmail =
                email.trim().toLowerCase(Locale.ROOT);

        Object verifiedEmail =
                session.getAttribute(
                        "passwordResetVerifiedEmail");

        if (verifiedEmail == null
                || !cleanEmail.equals(
                        verifiedEmail.toString())) {

            ra.addFlashAttribute(
                    "error",
                    "Please verify the password-reset OTP first.");

            return "redirect:/forgot-password";
        }

        PasswordResetChallenge challenge =
                resetChallenges
                        .findTopByEmailOrderByIdDesc(cleanEmail)
                        .orElse(null);

        User user =
                users.findByEmail(cleanEmail)
                        .orElse(null);

        if (challenge == null
                || !challenge.isUsed()
                || otpService.expired(
                        challenge.getExpiresAt())) {

            ra.addFlashAttribute(
                    "error",
                    "Your password-reset authorization has expired.");

            session.removeAttribute(
                    "passwordResetVerifiedEmail");

            return "redirect:/forgot-password";
        }

        if (user == null) {

            ra.addFlashAttribute(
                    "error",
                    "That email address is not registered.");

            session.removeAttribute(
                    "passwordResetVerifiedEmail");

            return "redirect:/forgot-password";
        }

        if (password.length() < 8) {

            ra.addFlashAttribute(
                    "error",
                    "Password must contain at least 8 characters.");

            ra.addFlashAttribute(
                    "email",
                    cleanEmail);

            return "redirect:/forgot-password/reset";
        }

        if (!password.equals(confirmPassword)) {

            ra.addFlashAttribute(
                    "error",
                    "The passwords do not match.");

            ra.addFlashAttribute(
                    "email",
                    cleanEmail);

            return "redirect:/forgot-password/reset";
        }

        user.changePassword(
                encoder.encode(password));

        users.save(user);

        resetChallenges.delete(challenge);

        session.removeAttribute(
                "passwordResetVerifiedEmail");

        ra.addFlashAttribute(
                "ok",
                "Password changed successfully. Please log in.");

        return "redirect:/login";
    }

    private boolean isEmailSyntaxValid(
            String email) {

        return email.matches(
                "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+"
                + "@[A-Za-z0-9]"
                + "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
                + "(?:\\.[A-Za-z0-9]"
                + "(?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");
    }
}
