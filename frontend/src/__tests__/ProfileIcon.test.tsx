import React from 'react';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import ProfileIcon from '../components/common/ProfileIcon';

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('ProfileIcon Component', () => {
  test('should render default profile picture when src is not provided', () => {
    render(<ProfileIcon size="md" />);
    const img = screen.getByAltText('profile');
    expect(img).toHaveAttribute(
      'src',
      'http://localhost:8080/profile_pictures/default_pfp.jpg'
    );
  });

  test('should render custom profile picture with BASE_URL prepended', () => {
    const customPic = '/images/profile_pictures/user123.jpg';
    render(<ProfileIcon size="md" src={customPic} />);
    const img = screen.getByAltText('profile');
    expect(img).toHaveAttribute(
      'src',
      `http://localhost:8080${customPic}`
    );
  });

  test('should apply correct size classes for different sizes', () => {
    const { container: containerSm } = render(<ProfileIcon size="sm" />);
    const { container: containerLg } = render(<ProfileIcon size="lg" />);
    
    expect(containerSm.querySelector('div')).toHaveClass('w-10', 'h-10');
    expect(containerLg.querySelector('div')).toHaveClass('w-28', 'h-28');
  });

  test('should be clickable and navigate to profile', () => {
    renderWithRouter(
      <ProfileIcon size="md" userId="user123" clickable={true} />
    );
    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/profile/user123');
  });

  test('should not render link when not clickable', () => {
    renderWithRouter(
      <ProfileIcon size="md" userId="user123" clickable={false} />
    );
    expect(screen.queryByRole('link')).not.toBeInTheDocument();
  });
});